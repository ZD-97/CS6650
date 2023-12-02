package org.example;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.DeliverCallback;

public class ReviewWorker {
  private static RMQChannelPool channelPool;
  private static String queueName = "reviewQueue";
  protected static void setupChannelPool(String hostname) throws Exception {
    ConnectionFactory factory = new ConnectionFactory();
    factory.setHost(hostname); // Or the appropriate host
    Connection connection = factory.newConnection();
    RMQChannelFactory rmqFactory = new RMQChannelFactory(connection);
    channelPool = new RMQChannelPool(300, rmqFactory); // Pool size of 10, for example
    declareQueue();
  }


  private static void declareQueue() throws Exception {
    try (Channel channel = channelPool.borrowObject()) {
      channel.queueDeclare(queueName, false, false, false, null);
      // No need to return the channel to the pool, as it is autoclosed by try-with-resources
    }
  }

  protected static void startConsumer() throws Exception {
    Channel channel = channelPool.borrowObject();
    try {
      channel.basicQos(1);
      DeliverCallback deliverCallback = (consumerTag, delivery) -> {
        // Process the message
        // Example: String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
        String message = new String(delivery.getBody(), "UTF-8");
        // SQL
        String[] messageParts = message.split(",");
        // Acknowledge the message
//        channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
//        ReviewServlet.postReviewFromDatabase(messageParts[0],messageParts[1]);
      };

      channel.basicConsume(queueName, true, deliverCallback, consumerTag -> {});
    } catch (RuntimeException e) {
      System.out.println(" [.] " + e);
    } finally {
      channelPool.returnObject(channel);
    }
  }
}
