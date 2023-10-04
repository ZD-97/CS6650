package org.example;

import java.util.concurrent.CountDownLatch;

public class RequestCounterBarrier {

  final static private int NUMTHREADS = 1000;
  private int count = 0;

  synchronized public void inc() {
    count++;
  }

  public int getVal() {
    return this.count;
  }

  public static void main(String[] args) throws InterruptedException {
    final RequestCounterBarrier counter = new RequestCounterBarrier();
    CountDownLatch completed = new CountDownLatch(NUMTHREADS);
    for (int i = 0; i < NUMTHREADS; i++) {
// lambda runnable creation - interface only has a single method so lambda works fine
      Runnable thread = () -> {
        counter.inc();
        completed.countDown();
      };
      new Thread(thread).start();
    }
    completed.await();
    System.out.println("Value should be equal to " + NUMTHREADS + " It is: " + counter.getVal());
  }
}

