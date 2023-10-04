package org.example;
import java.sql.Timestamp;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Counter {
  final static private int NUMTHREADS = 1000000;
  private final AtomicInteger count= new AtomicInteger();
  public void inc() {
    count.getAndIncrement();
  }
  public int getVal() {
    return this.count.get();
  }
  public static void main(String[] args) {
    Counter counter = new Counter();
    long start = System.currentTimeMillis();
    CountDownLatch latch = new CountDownLatch(NUMTHREADS);
    for (int i = 0; i < NUMTHREADS; i++) {
// lambda runnable creation - interface only has a single method so lambda works fine
      Runnable thread = () -> {
        for(int j=0;j<10;j++){counter.inc();}
        latch.countDown();
      };
      new Thread(thread).start();
    }
    long end = System.currentTimeMillis();
    System.out.println(end-start);
  }
}
