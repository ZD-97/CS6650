import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.http.*;
import java.net.URI;
import java.net.http.HttpRequest.BodyPublishers;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

public class LoadTestClient {

  private static final int INITIAL_THREADS = 10;
  private static final int API_CALLS_PER_THREAD = 1000;

  private static final AtomicLong successfulRequests = new AtomicLong(0);
  private static final AtomicLong failedRequests = new AtomicLong(0);


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
      initService.submit(new WorkerThread(ipAddr, 100,successfulRequests,failedRequests));
    }
    initService.shutdown();
    initService.awaitTermination(1, TimeUnit.HOURS);

    System.out.println("Initialization Phase finished");

    long startTime = System.currentTimeMillis();

    // Test Phase
    ExecutorService mainService = Executors.newFixedThreadPool(threadGroupSize * numThreadGroups);
    for (int i = 0; i < numThreadGroups; i++) {
      for (int j = 0; j < threadGroupSize; j++) {
        mainService.submit(new WorkerThread(ipAddr, API_CALLS_PER_THREAD,successfulRequests,failedRequests));
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
    System.out.println("Number of successful requests: " + successfulRequests.get());
    System.out.println("Number of failed requests: " + failedRequests.get());
  }
}

class WorkerThread implements Runnable {
  private final int apiCalls;
  private final APIClient client;
  private final String ipAddr;

  public WorkerThread(String ipAddr, int apiCalls,AtomicLong success,AtomicLong fail) {
    this.ipAddr = ipAddr;
    this.apiCalls = apiCalls;
    this.client = new APIClient(ipAddr,success,fail);
  }

  @Override
  public void run() {
    for (int i = 0; i < apiCalls; i++) {
      client.post();
      client.get();
    }
  }
}

class  APIClient {
  private final String ipAddr;
  private final HttpClient httpClient;
  private final AtomicLong successfulRequests;
  private final AtomicLong failedRequests;
  private byte[] imageBytes;
  public APIClient(String ipAddr, AtomicLong successfulRequests, AtomicLong failedRequests) {
    this.ipAddr = ipAddr;
    this.httpClient = HttpClient.newHttpClient();
    this.successfulRequests = successfulRequests;
    this.failedRequests = failedRequests;
  }

  public void post() {
    String boundary = UUID.randomUUID().toString();
    String jsonString = "{\n" +
        "    \"artist\": \"Da Zhu\",\n" +
        "    \"title\": \"Never Mind The Bollocks!\",\n" +
        "    \"year\": \"1977\"\n" +
        "}";

    String albumDataPart = "--" + boundary + "\r\n"
        + "Content-Disposition: form-data; name=\"albumData\"\r\n\r\n"
        + jsonString + "\r\n";

    String imageHeader = "--" + boundary + "\r\n"
        + "Content-Disposition: form-data; name=\"image\"; filename=\"filename.jpg\"\r\n"
        + "Content-Type: image/jpeg\r\n\r\n";
    if(imageBytes == null) {
      try {
        imageBytes = Files.readAllBytes(Path.of("/Users/zhuda/Desktop/Align/CS6650/HW2/HW2(Client)/Trump.jpeg"));
      } catch (IOException e) {
        throw new RuntimeException("Failed to read image file", e);
      }
    }

    String endBoundary = "\r\n--" + boundary + "--\r\n";

    List<byte[]> byteArrays = Arrays.asList(albumDataPart.getBytes(), imageHeader.getBytes(), imageBytes, endBoundary.getBytes());

    HttpRequest.BodyPublisher publisher = HttpRequest.BodyPublishers.ofByteArrays(byteArrays);

    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(ipAddr))
        .POST(publisher)
        .header("Content-Type", "multipart/form-data;boundary=" + boundary)  // Set the header here
        .build();

    executeHttpRequest(request);
  }




  public void get() {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(ipAddr))
        .GET()
        .build();
    executeHttpRequest(request);
  }
  private void executeHttpRequest(HttpRequest request) {
    for (int i = 0; i < 5; i++) {
      try {
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        int statusCode = response.statusCode();
        if (statusCode >= 200 && statusCode < 400) {
          successfulRequests.incrementAndGet();
          break; // successful request, break out of retry loop
        }
        if (statusCode >= 400 && statusCode < 500) {
          failedRequests.incrementAndGet();
          System.err.println("Client error: " + statusCode);
          break; // client error, break out of retry loop
        }
        // If it's a 5XX error, it'll continue to the next iteration to retry
      } catch (Exception e) {
        System.err.println("Error: " + e.getMessage());
      }
    }
  }
}

