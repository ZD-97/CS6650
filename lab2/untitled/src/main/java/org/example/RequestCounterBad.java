package org.example;

import java.util.concurrent.atomic.AtomicInteger;

public class RequestCounterBad {
  final static private int NUMTHREADS = 100000;
  private final AtomicInteger count= new AtomicInteger();
  public void inc() {
    count.getAndIncrement();
  }
  public int getVal() {
    return this.count.get();
  }
  public static void main(String[] args) throws InterruptedException {
    final RequestCounterBad counter = new RequestCounterBad();
    for (int i = 0; i < NUMTHREADS; i++) {
// lambda runnable creation - interface only has a single method so lambda works fine
      Runnable thread = () -> { counter.inc(); };
      new Thread(thread).start();
    }
    Thread.sleep(5000);
    System.out.println("Value should be equal to " + NUMTHREADS + " It is: " + counter.getVal());
  }
}

