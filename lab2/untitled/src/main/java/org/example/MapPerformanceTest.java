package org.example;

import java.util.*;
import java.util.concurrent.*;

public class MapPerformanceTest {

  private static final int NUM_ELEMENTS = 100000;
  private static final int NUM_THREADS = 100;

  public static void main(String[] args) throws InterruptedException {
    singleThreadedTest();
    multiThreadedTest();
  }

  private static void singleThreadedTest() {
    System.out.println("Single-threaded Test:");

    // Test with HashTable
    Hashtable<Integer, Integer> hashTable = new Hashtable<>();
    long durationHashTable = timeInsertion(hashTable, NUM_ELEMENTS);
    System.out.println("Time taken to add to HashTable: " + durationHashTable + " nanoseconds.");

    // Test with HashMap
    HashMap<Integer, Integer> hashMap = new HashMap<>();
    long durationHashMap = timeInsertion(hashMap, NUM_ELEMENTS);
    System.out.println("Time taken to add to HashMap: " + durationHashMap + " nanoseconds.");

    // Test with ConcurrentHashMap
    ConcurrentHashMap<Integer, Integer> concurrentHashMap = new ConcurrentHashMap<>();
    long durationConcurrentHashMap = timeInsertion(concurrentHashMap, NUM_ELEMENTS);
    System.out.println("Time taken to add to ConcurrentHashMap: " + durationConcurrentHashMap + " nanoseconds.");
  }

  private static void multiThreadedTest() throws InterruptedException {
    System.out.println("\nMulti-threaded Test:");

    Hashtable<Integer, Integer> hashTable = new Hashtable<>();
    long durationHashTable = timeInsertionMultiThreaded(hashTable, NUM_ELEMENTS, NUM_THREADS);
    System.out.println("Time taken to add to HashTable: " + durationHashTable + " nanoseconds.");

    Map<Integer, Integer> synchronizedHashMap = Collections.synchronizedMap(new HashMap<>());
    long durationSynchronizedHashMap = timeInsertionMultiThreaded(synchronizedHashMap, NUM_ELEMENTS, NUM_THREADS);
    System.out.println("Time taken to add to synchronized HashMap: " + durationSynchronizedHashMap + " nanoseconds.");

    ConcurrentHashMap<Integer, Integer> concurrentHashMap = new ConcurrentHashMap<>();
    long durationConcurrentHashMap = timeInsertionMultiThreaded(concurrentHashMap, NUM_ELEMENTS, NUM_THREADS);
    System.out.println("Time taken to add to ConcurrentHashMap: " + durationConcurrentHashMap + " nanoseconds.");
  }

  private static long timeInsertion(Map<Integer, Integer> map, int numElements) {
    long startTime = System.nanoTime();
    for (int i = 0; i < numElements; i++) {
      map.put(i, i);
    }
    long endTime = System.nanoTime();
    return endTime - startTime;
  }

  private static long timeInsertionMultiThreaded(Map<Integer, Integer> map, int numElements, int numThreads) throws InterruptedException {
    ExecutorService executor = Executors.newFixedThreadPool(numThreads);
    long startTime = System.nanoTime();

    for (int t = 0; t < numThreads; t++) {
      executor.execute(() -> {
        for (int i = 0; i < numElements / numThreads; i++) {
          map.put(i, i);
        }
      });
    }

    executor.shutdown();
    executor.awaitTermination(1, TimeUnit.MINUTES);

    long endTime = System.nanoTime();
    return endTime - startTime;
  }
}

