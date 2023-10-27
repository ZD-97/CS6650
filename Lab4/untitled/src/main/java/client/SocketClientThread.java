package client;

/**
 * Simple skeleton socket client thread that coordinates termination
 * with a cyclic barrier to demonstration barrier synchronization
 * @author Ian Gorton
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Logger;
import java.util.logging.Level;

// Sockets of this class are coordinated  by a CyclicBarrier which pauses all threads
// until the last one completes. At this stage, all threads terminate

public class SocketClientThread extends Thread {
  private long clientID;
  String hostName;
  int port;
  CyclicBarrier synk;
  int NUM_ITERATIONS = 1000;
  public SocketClientThread(String hostName, int port, CyclicBarrier barrier) {
    this.hostName = hostName;
    this.port = port;

    synk = barrier;

  }

  public void run() {
    clientID = Thread.currentThread().getId();
    Socket s;
    for (int i = 0; i < NUM_ITERATIONS; i++) {
      try {
        // TO DO insert code to pass 1k messages to the SocketServer
        s = new Socket(hostName, port);
        PrintWriter out =
            new PrintWriter(s.getOutputStream(), true);
        BufferedReader in =
            new BufferedReader(
                new InputStreamReader(s.getInputStream()));
        out.println("Client ID is " +  Long.toString(clientID));
        System.out.println(in.readLine());
      } catch (UnknownHostException e) {
        System.err.println("Don't know about host " + hostName);
        break;
      } catch (IOException e) {
        System.err.println("Couldn't get I/O for the connection to " +
            hostName);
        break;
      }
    }
    // TO DO insert code to wait on the CyclicBarrier
    try {
      // TO DO insert code to wait on the CyclicBarrier
      System.out.println("Thread waiting at barrier");
      synk.await();
    } catch (InterruptedException | BrokenBarrierException ex) {
      Logger.getLogger(SocketClientThread.class.getName()).log(Level.SEVERE, null, ex);
    }
  }
}
