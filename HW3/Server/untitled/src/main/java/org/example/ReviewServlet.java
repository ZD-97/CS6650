package org.example;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import javax.servlet.*;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.*;
import java.io.IOException;
import com.google.gson.Gson;
import com.rabbitmq.client.*;
import com.rabbitmq.client.Channel;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

@WebServlet(name = "org.example.ReviewServlet",value = "/review/*")
public class ReviewServlet extends HttpServlet {
  private Gson gson = new Gson();
  private RMQChannelPool channelPool = null;
  private String hostname = "34.222.124.45";
  private int numOfConsumer = 250;
  private ExecutorService consumerService = Executors.newFixedThreadPool(numOfConsumer);
  @Override
  public void init() throws ServletException {
    try {
      ConnectionFactory factory = new ConnectionFactory();
      factory.setHost(hostname); // Or the appropriate host
      com.rabbitmq.client.Connection connection = factory.newConnection();
      RMQChannelFactory channelFactory = new RMQChannelFactory(connection);
      channelPool = new RMQChannelPool(300, channelFactory);
//      ReviewWorker.setupChannelPool(hostname);
      for(int i=0;i<numOfConsumer;i++) {
        consumerService.submit(new Thread(new ReviewRunnable(channelPool)));
      }
      consumerService.shutdown();
    } catch (Exception e) {
      e.printStackTrace();
      throw new ServletException("Failed to connect to RabbitMQ", e);
    }
  }

  @Override
  protected void doPost(HttpServletRequest req, HttpServletResponse res)
      throws ServletException, IOException {
    res.setContentType("application/json");
    res.setCharacterEncoding("UTF-8");
    String urlPath = req.getPathInfo();
    if (urlPath == null || urlPath.isEmpty()) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      res.getWriter().write("Missing parameters");
      return;
    }
    String[] urlParts = urlPath.split("/");
    if (!isGetUrlValid(urlParts,"post")) {
      res.setStatus(HttpServletResponse.SC_NOT_FOUND);
      errorMsg getErrorMsgGet = new errorMsg("You need to follow the post format");
      res.getWriter().write(this.gson.toJson(getErrorMsgGet));
    } else {
      //TODO: 1.if we cant find the id in database return error message
      //TODO: 2.otherwise set the likeordislike field accordingly

      String albumId = urlParts[2];
      String likeaction = urlParts[1];
      Channel rabbitMQChannel = null;
      try {
        String message = albumId + "," + likeaction;
        rabbitMQChannel = channelPool.borrowObject();
        rabbitMQChannel.basicPublish("", "reviewQueue", null, message.getBytes("UTF-8"));
        res.getWriter().write(this.gson.toJson(new errorMsg("Review update request submitted")));
      } catch (IOException e) {
        e.printStackTrace();
        res.getWriter().write(this.gson.toJson(new errorMsg("Error submitting review update")));
      } finally {
        if (rabbitMQChannel != null) {
          try {
            channelPool.returnObject(rabbitMQChannel);
          } catch (Exception e) {
            throw new RuntimeException(e);
          }
        }
      }
//      errorMsg msg = new errorMsg("You liked this album");
//      if(likeaction.equals("dislike")) {
//        res.setStatus(HttpServletResponse.SC_OK);
//        msg = new errorMsg("You disliked this album");
//      }
//      res.getWriter().write(this.gson.toJson(msg));
    }
  }

  private boolean fetchReviewFromDatabase(String albumId) {
    String sql = "SELECT artist, title, year, like FROM albums WHERE id = ?"; // Assuming your table has columns: artist, title, year
    try (Connection connection = DatabaseUtil.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {

      statement.setInt(1, Integer.parseInt(albumId));

      try (ResultSet resultSet = statement.executeQuery()) {
        if (resultSet.next()) {
          boolean likeInfo = resultSet.getBoolean("like");
          return  likeInfo;
        }
      }
    } catch (SQLException | ClassNotFoundException e) {
      e.printStackTrace(); // You might want to log the error in a production application
    }
    return false; // Return null if album is not found or if there's any exception
  }

//  protected static boolean postReviewFromDatabase(String albumId, String likeAction) {
//    String columnToUpdate = likeAction.equals("like") ? "`like`" : "dislike";
//    String sql = "UPDATE albums SET " + columnToUpdate + " = " + columnToUpdate + " + 1 WHERE id = ?";
//
//    try (Connection connection = DatabaseUtil.getConnection();
//        PreparedStatement statement = connection.prepareStatement(sql)) {
//      statement.setInt(1, Integer.parseInt(albumId));
//      int rowsUpdated = statement.executeUpdate();
//      return rowsUpdated > 0; // Returns true if the update was successful
//    } catch (SQLException | ClassNotFoundException e) {
//      e.printStackTrace(); // Consider using a logging framework here
//      return false;
//    }
//  }

  private boolean isGetUrlValid(String[] urlPath,String method) {
    // TODO: validate the request url path according to the API spec
    // urlPath  = "/1/seasons/2019/day/1/skier/123"
    // urlParts = [, 1, seasons, 2019, day, 1, skier, 123]
    if(method.equals("post")) {
      if(urlPath.length == 3 && (urlPath[1].equals("like") || urlPath[1].equals("dislike"))) return true;
      else {
        return false;
      }
    } else {
      if(urlPath.length == 2 && urlPath[1].length() > 0) return true;
      else {
        return false;
      }
    }
  }
}
