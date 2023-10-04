import java.util.concurrent.CountDownLatch;
import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.*;
import org.apache.commons.httpclient.params.HttpMethodParams;

import java.io.*;

public class HttpClientTutorial {
  final static private int NUMTHREADS = 100;
  private static String url = "http://localhost:8080/Lab3_war_exploded/hello";

  public static void main(String[] args) throws InterruptedException {

    CountDownLatch completed = new CountDownLatch(NUMTHREADS);
    long start = System.currentTimeMillis();
    for (int i = 0; i < NUMTHREADS; i++) {
      // lambda runnable creation - interface only has a single method so lambda works fine
      Runnable thread =  () -> {
        HttpClient client = new HttpClient();

        // Create a method instance.
        GetMethod method = new GetMethod(url);

        // Provide custom retry handler is necessary
        method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
            new DefaultHttpMethodRetryHandler(3, false));
        try {
        // Create an instance of HttpClient.

        // Execute the method.
        int statusCode = client.executeMethod(method);

        if (statusCode != HttpStatus.SC_OK) {
          System.err.println("Method failed: " + method.getStatusLine());
        }

        // Read the response body.
        byte[] responseBody = method.getResponseBody();

        // Deal with the response.
        // Use caution: ensure correct character encoding and is not binary data
        System.out.println(new String(responseBody));

      } catch (HttpException e) {
        System.err.println("Fatal protocol violation: " + e.getMessage());
        e.printStackTrace();
      } catch (IOException e) {
        System.err.println("Fatal transport error: " + e.getMessage());
        e.printStackTrace();
      } finally {
        method.releaseConnection();
        completed.countDown();
      }
      };
      new Thread(thread).start();
    }
    completed.await();
    long end = System.currentTimeMillis();
    System.out.println(end-start);
  }
}