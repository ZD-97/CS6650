package org.example;

import java.util.ArrayList;
import java.util.Vector;

public class Collections1 {

  public static void main(String[] args) {
    final int NUM_ELEMENTS = 100000;

    // Test with Vector
    Vector<Integer> vector = new Vector<>();
    long startTime = System.nanoTime();
    for (int i = 0; i < NUM_ELEMENTS; i++) {
      vector.add(i);
    }
    long endTime = System.nanoTime();
    long durationVector = endTime - startTime;
    System.out.println("Time taken to add " + NUM_ELEMENTS + " elements to Vector: " + durationVector + " nanoseconds.");

    // Test with ArrayList
    ArrayList<Integer> arrayList = new ArrayList<>();
    startTime = System.nanoTime();
    for (int i = 0; i < NUM_ELEMENTS; i++) {
      arrayList.add(i);
    }
    endTime = System.nanoTime();
    long durationArrayList = endTime - startTime;
    System.out.println("Time taken to add " + NUM_ELEMENTS + " elements to ArrayList: " + durationArrayList + " nanoseconds.");

    // Comparison
    System.out.println("Difference (Vector - ArrayList): " + (durationVector - durationArrayList) + " nanoseconds.");
  }
}

