package com.husen.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.Properties;

/**
* 聊天的客户端
 * @author 11785
 */
public class Server {
    private static final String EXCHANGE_NAME = "talkroomRedis";
    private static Connection connection;
    private static Channel channel;
        static {
            Properties properties = new Properties();
            try {
                properties.load(Server.class.getClassLoader().getResourceAsStream("rabbitmq-talkroom.properties"));
                ConnectionFactory connectionFactory = new ConnectionFactory();
                connectionFactory.setHost(properties.getProperty("rabbitmq.host"));
                connectionFactory.setPort(Integer.valueOf(properties.getProperty("rabbitmq.port")));
                connectionFactory.setUsername(properties.getProperty("rabbitmq.username"));
                connectionFactory.setPassword(properties.getProperty("rabbitmq.password"));
                connectionFactory.setVirtualHost(properties.getProperty("rabbitmq.virtualHost"));
                connection = connectionFactory.newConnection();
                channel = connection.createChannel();
                channel.exchangeDeclare(EXCHANGE_NAME, "fanout");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    public static void sendMessage(String message){
            if(!StringUtils.isBlank(message)){
                try {
                    channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
    }
    public static void closeTalkRoom(){
        try {
            if(channel != null){
                channel.close();
                connection.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void main(String[] args){
        while (true){
            Server.sendMessage("husen");
            try {
                Thread.sleep(10000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
