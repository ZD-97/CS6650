package Client2;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.net.http.*;
import java.net.URI;
import java.util.Queue;
import java.util.concurrent.*;

public class LoadTestClient {

  private static final int INITIAL_THREADS = 10;
  private static final int API_CALLS_PER_THREAD = 1000;
  private static BufferedWriter writer;
  private static final String OUTPUT_FILE = "output.csv";
  private static Queue<String> globalQueue = new ConcurrentLinkedQueue<>();

  private static void logRecord() throws IOException {
    writer = new BufferedWriter(new FileWriter(OUTPUT_FILE, true));
    for(String record:globalQueue) {
      try {
        writer.write(record);
        writer.flush();
      } catch (IOException e) {
        System.err.println("Failed to log request: " + e.getMessage());
      }
    }
  }

  public static void main(String[] args) throws Exception {
    if (args.length != 4) {
      System.out.println("Usage: LoadTestClient <threadGroupSize> <numThreadGroups> <delay> <IPAddr>");
      return;
    }

    int threadGroupSize = Integer.parseInt(args[0]);
    int numThreadGroups = Integer.parseInt(args[1]);
    int delay = Integer.parseInt(args[2]);
    String ipAddr = args[3];

    // Initialization Phase
    ExecutorService initService = Executors.newFixedThreadPool(INITIAL_THREADS);
    for (int i = 0; i < INITIAL_THREADS; i++) {
      initService.submit(new WorkerThread(ipAddr, 100,globalQueue));
    }
    initService.shutdown();
    initService.awaitTermination(1, TimeUnit.HOURS);

    System.out.println("Initialization Phase finished, The Url is "+ ipAddr);

    long startTime = System.currentTimeMillis();

    // Test Phase
    ExecutorService mainService = Executors.newFixedThreadPool(threadGroupSize * numThreadGroups);
    for (int i = 0; i < numThreadGroups; i++) {
      for (int j = 0; j < threadGroupSize; j++) {
        mainService.submit(new WorkerThread(ipAddr, API_CALLS_PER_THREAD,globalQueue));
      }
      if (i < numThreadGroups - 1) { // don't delay after the last group
        Thread.sleep(delay * 1000L);
      }
    }
    mainService.shutdown();
    mainService.awaitTermination(1, TimeUnit.HOURS);

    long endTime = System.currentTimeMillis();
    double wallTime = (endTime-startTime) / 1000;
    long totalRequests = (long) threadGroupSize * numThreadGroups * API_CALLS_PER_THREAD;
    double throughput = totalRequests / wallTime;

    System.out.println("Wall Time: " + wallTime + " seconds");
    System.out.println("Throughput: " + throughput + " requests per second");
    logRecord();
    StatesCalculator.computeStats("output.csv");
  }
}

class WorkerThread implements Runnable {
  private final String ipAddr;
  private final int apiCalls;
  private final APIClient client;

  public WorkerThread(String ipAddr, int apiCalls, Queue<String> que) throws IOException {
    this.ipAddr = ipAddr;
    this.apiCalls = apiCalls;
    this.client = new APIClient(ipAddr,que);
  }

  @Override
  public void run() {
    for (int i = 0; i < apiCalls; i++) {
      client.post();
      client.get();
    }
  }
}

class APIClient {
  private final String ipAddr;
  private final HttpClient httpClient;
  private Queue<String> records;

  public APIClient(String ipAddr,Queue<String> queue) throws IOException {
    this.ipAddr = ipAddr;
    this.httpClient = HttpClient.newHttpClient();
    this.records = queue;
  }

  public void post() {
    executeRequest("POST");
  }

  public void get() {
    executeRequest("GET");
  }

  private void executeRequest(String method) {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(ipAddr))
        .method(method, HttpRequest.BodyPublishers.noBody())
        .build();
    long start = System.currentTimeMillis();
    for (int i = 0; i < 5; i++) {
      try {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        long end = System.currentTimeMillis();
        long latency = end - start;
        int statusCode = response.statusCode();
        addRecord(start,method,latency,statusCode);
        if (statusCode >= 200 && statusCode < 400) {
          break; // successful request, break out of retry loop
        }
        if (statusCode >= 400 && statusCode < 500) {
          System.err.println("Client error: " + statusCode);
          break; // client error, break out of retry loop
        }
        // If it's a 5XX error, it'll continue to the next iteration to retry
      } catch (Exception e) {
        System.err.println("Error: " + e.getMessage());
      }
    }
  }

  private void addRecord(long start, String method, long latency, int statusCode) {
     String record = String.format("%s,%s,%d,%d\n", start, method, latency, statusCode);
     this.records.add(record);
  }
}