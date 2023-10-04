package org.example;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class FileWriting {
  private static final int NUM_THREADS = 500;
  private static final int NUM_STRINGS = 1000;
  private static final int QUEUE_CAPACITY = 20000;
  private static long fileWritingMultiThreaded1(int numThreads) throws InterruptedException {
    //write every string to the file immediately after it is generated in the loop in each thread
    ExecutorService executor = Executors.newFixedThreadPool(numThreads);
    long startTime = System.nanoTime();
    String fileName = "output.txt";
    Path filePath = Paths.get(fileName);
    for (int t = 0; t < numThreads; t++) {
      executor.execute(() -> {
        for (int i = 0; i < NUM_STRINGS; i++) {
          String str = String.format("%d,%d,%d\n",System.currentTimeMillis(),Thread.currentThread().getId(),i);
          try {
            Files.write(filePath, str.getBytes(),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
          } catch (IOException e) {
            e.printStackTrace();
          }
        }
      });
    }
    executor.shutdown();
    executor.awaitTermination(1, TimeUnit.MINUTES);
    long endTime = System.nanoTime();
    return endTime-startTime;
  }

  private static long fileWritingMultiThreaded2(int numThreads) throws InterruptedException {
    //write every string to the file immediately after it is generated in the loop in each thread
    ExecutorService executor = Executors.newFixedThreadPool(numThreads);
    long startTime = System.nanoTime();
    String fileName = "output.txt";
    Path filePath = Paths.get(fileName);

    for (int t = 0; t < numThreads; t++) {
      executor.execute(() -> {
        List<String> list = new CopyOnWriteArrayList<>();;
        for (int i = 0; i < NUM_STRINGS; i++) {
          String str = String.format("%d,%d,%d\n",System.currentTimeMillis(),Thread.currentThread().getId(),i);
          list.add(str);
        }
        try {
          Files.write(filePath, list,
              StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
          e.printStackTrace();
        }
      });
    }
    executor.shutdown();
    executor.awaitTermination(1, TimeUnit.MINUTES);
    long endTime = System.nanoTime();
    return endTime-startTime;
  }

  private static long fileWritingMultiThreaded3(int numThreads) throws InterruptedException {
    //write every string to the file immediately after it is generated in the loop in each thread
    ExecutorService executor = Executors.newFixedThreadPool(numThreads);
    long startTime = System.nanoTime();
    String fileName = "output.txt";
    Path filePath = Paths.get(fileName);
    List<String> list = new CopyOnWriteArrayList<>();;
    for (int t = 0; t < numThreads; t++) {
      executor.execute(() -> {
        for (int i = 0; i < NUM_STRINGS; i++) {
          String str = String.format("%d,%d,%d",System.currentTimeMillis(),Thread.currentThread().getId(),i);
          list.add(str);
        }
      });
    }
    executor.shutdown();
    executor.awaitTermination(1, TimeUnit.MINUTES);
    try {
      Files.write(filePath, list,
          StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
    } catch (IOException e) {
      e.printStackTrace();
    }
    long endTime = System.nanoTime();
    return endTime-startTime;
  }

  private static long fileWritingMultiThreaded4(int numThreads) throws InterruptedException {
    //write every string to the file immediately after it is generated in the loop in each thread
    ExecutorService executor = Executors.newFixedThreadPool(numThreads+1);
    BlockingQueue<String> queue = new LinkedBlockingQueue<>(QUEUE_CAPACITY);
    long startTime = System.nanoTime();
    String fileName = "output.txt";

    List<String> list = new CopyOnWriteArrayList<>();;

    executor.execute(() -> {
      Path filePath = Paths.get(fileName);
      try {
        while (true) {
          String str = queue.take(); // Blocks if the queue is empty
          if (str.equals("END")) break; // End signal
          Files.write(filePath,  str.getBytes(),
              StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
        }
      } catch (IOException | InterruptedException e) {
        e.printStackTrace();
      }
    });

    for (int t = 0; t < numThreads; t++) {
      executor.execute(() -> {
        for (int i = 0; i < NUM_STRINGS; i++) {
          String str = String.format("%d,%d,%d",System.currentTimeMillis(),Thread.currentThread().getId(),i);
          try {
            queue.put(str); // Blocks if the queue is full
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
      });
    }
    executor.shutdown();
    executor.awaitTermination(1, TimeUnit.MINUTES);

    long endTime = System.nanoTime();
    return endTime-startTime;
  }

  public static void main(String[] args) throws InterruptedException {
//    long time1 = fileWritingMultiThreaded1(NUM_THREADS);
//    long time2 = fileWritingMultiThreaded2(NUM_THREADS);
//    long time3 = fileWritingMultiThreaded3(NUM_THREADS);
    long time4 = fileWritingMultiThreaded4(NUM_THREADS);
    System.out.println(time4);
//    System.out.println(String.format("Time1 is %d\nTime2 is %d\nTime3 is %d\nTime4 is %d",time1,time2,time3,time4));
  }
}
