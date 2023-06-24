package function;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

import java.nio.charset.StandardCharsets;


public class sender {
    private enum EXCHANGE_TYPE {DIRECT, FANOUT, TOPIC, HEADERS}

    private final static String EXCHANGE_NAME = "hello";

    // Set this for topic or direct exchanges. Leave empty for fanout.
    private final static String TOPIC_KEY_NAME = ""; // For topic the format is keyword1.keyword2.keyword3. and so on.

    public static void sending(String message) throws Exception {

        // Connect to the RabbitMQ server
        ConnectionFactory factory = new ConnectionFactory();
        // Connection credentials. Must be added for both publisher and subscriber
        factory.setHost("51.105.45.183"); // Add here the IP provided by your tutor
        factory.setPort(5672);
        factory.setUsername("user"); // Add here the username provided by your tutor
        factory.setPassword("pass"); // Add here the password provided by your tutor

        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            //channel.exchangeDelete(EXCHANGE_NAME); // sometimes you must delete an existing exchange
            // Declare the exchange you want to connect your queue to
            channel.exchangeDeclare(EXCHANGE_NAME, EXCHANGE_TYPE.FANOUT.toString().toLowerCase()); // 2nd parameter: fanout, direct, topic, headers

            // Publish a message to the exchange
            // This message will remain there until a client consumes it ...
            // Notice any difference in behavior as opposed to our previous socket client-server app?
            channel.basicPublish(EXCHANGE_NAME,
                    TOPIC_KEY_NAME, // This parameter is used for the routing key, which is usually used for direct or topic queues.
                    new AMQP.BasicProperties.Builder()
                            .contentType("text/plain")
                            .deliveryMode(2)
                            .priority(1)
                            .userId("user")
                            //.expiration("60000")
                            .build(),
                    message.getBytes(StandardCharsets.UTF_8));
            System.out.println(" [x] Sent '" + TOPIC_KEY_NAME + ":" + message + "'");
        }
    }
}
