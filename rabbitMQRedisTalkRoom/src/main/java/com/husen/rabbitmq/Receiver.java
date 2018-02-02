package com.husen.rabbitmq;

import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

/**
* 接收消息的客户端
 * @author 11785
 */
public class Receiver {
    private static final String EXCHANGE_NAME = "talkroomRedis";
    private static String host;
    private static Integer port;
    private static String username;
    private static String password;
    private static String virtualHost;
    private Connection connection;
    private Channel channel;
    static {
        Properties properties = new Properties();
        try {
            properties.load(Server.class.getClassLoader().getResourceAsStream("rabbitmq-talkroom.properties"));
            host = properties.getProperty("rabbitmq.host");
            port = Integer.valueOf(properties.getProperty("rabbitmq.port"));
            username = properties.getProperty("rabbitmq.username");
            password = properties.getProperty("rabbitmq.password");
            virtualHost = properties.getProperty("rabbitmq.virtualHost");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void receive() throws IOException, TimeoutException {
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(host);
        connectionFactory.setPort(port);
        connectionFactory.setUsername(username);
        connectionFactory.setPassword(password);
        connectionFactory.setVirtualHost(virtualHost);
        connection = connectionFactory.newConnection();
        channel = connection.createChannel();
        channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
        //产生一个随机队列的名称
        String queueName = channel.queueDeclare().getQueue();
        //对队列进行绑定
        channel.queueBind(queueName, EXCHANGE_NAME, "");
        System.out.println("asdasds");
        Consumer consumer = new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                super.handleDelivery(consumerTag, envelope, properties, body);
                String message = new String(body, "utf-8");
                System.out.println(message);
            }
        };
        //队列会自动删除
        channel.basicConsume(queueName, true, consumer);
    }
    public void closeReceiver() throws IOException, TimeoutException {
        if(connection != null){
            channel.close();
            connection.close();
        }
    }

    public static void main(String[] args) throws IOException, TimeoutException {
        Receiver receiver = new Receiver();
        receiver.receive();
    }
}
