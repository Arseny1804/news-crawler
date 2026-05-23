package org.example.rabbit;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class RabbitMQService {

    private static final String HOST = "localhost";

    public static final String LINKS_QUEUE =
            "news_links";

    public static final String RESULTS_QUEUE =
            "news_results";

    private Connection connection;

    private Channel channel;

    public RabbitMQService() {

        try {

            ConnectionFactory factory =
                    new ConnectionFactory();

            factory.setHost(HOST);

            connection =
                    factory.newConnection();

            channel =
                    connection.createChannel();

            // Очередь ссылок
            channel.queueDeclare(
                    LINKS_QUEUE,
                    true,
                    false,
                    false,
                    null
            );

            // Очередь результатов
            channel.queueDeclare(
                    RESULTS_QUEUE,
                    true,
                    false,
                    false,
                    null
            );

            System.out.println(
                    "RabbitMQ подключен"
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public void sendLink(String link) {

        try {

            channel.basicPublish(
                    "",
                    LINKS_QUEUE,
                    null,
                    link.getBytes()
            );

            System.out.println(
                    "Ссылка отправлена: "
                            + link
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public void sendResult(String json) {

        try {

            channel.basicPublish(
                    "",
                    RESULTS_QUEUE,
                    null,
                    json.getBytes()
            );

            System.out.println(
                    "Результат отправлен"
            );

        } catch (Exception e) {

            e.printStackTrace();
        }
    }

    public Channel getChannel() {
        return channel;
    }
}