package org.example;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.DeliverCallback;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class ReviewRunnable implements Runnable{
  private final static String queueName = "reviewQueue";
  private RMQChannelPool channelPool = null;
  public ReviewRunnable(RMQChannelPool pool) {
    this.channelPool = pool;
  }
  @Override
  public void run() {
    Channel channel = null;
    try {
      channel = channelPool.borrowObject();
      channel.queueDeclare(queueName, false, false, false, null);
    } catch (IOException e) {
      System.out.println(" [.] " + e);
    }
    try {
      channel.basicQos(50);
      Channel finalChannel = channel;
      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        // Process the message
        // Example: String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        String message = new String(delivery.getBody(), "UTF-8");
        // SQL
        String[] messageParts = message.split(",");
        // Acknowledge the message
        finalChannel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
//        postReviewFromDatabase(messageParts[0],messageParts[1]);
      };
      channel.basicConsume(queueName, false, deliverCallback, consumerTag -> {});
    } catch (RuntimeException | IOException e) {
      System.out.println(" [.] " + e);
    } finally {
      try {
        channelPool.returnObject(channel);
      } catch (Exception e) {
        System.out.println(" [.] " + e);
      }
    }
  }

  public boolean postReviewFromDatabase(String albumId, String likeAction) {
    String columnToUpdate = likeAction.equals("like") ? "`like`" : "dislike";
    String sql = "UPDATE albums SET " + columnToUpdate + " = " + columnToUpdate + " + 1 WHERE id = ?";

    try (Connection connection = DatabaseUtil.getConnection();
        PreparedStatement statement = connection.prepareStatement(sql)) {
      statement.setInt(1, Integer.parseInt(albumId));
      int rowsUpdated = statement.executeUpdate();
      return rowsUpdated > 0; // Returns true if the update was successful
    } catch (SQLException | ClassNotFoundException e) {
      e.printStackTrace(); // Consider using a logging framework here
      return false;
    }
  }
}
