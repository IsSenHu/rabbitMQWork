package com.husen.rabbitmq;

import com.alibaba.fastjson.JSON;
import com.husen.pojo.TalkUser;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

/**
 * @author 11785
 */
public class RegistServer {
    private Connection connection;
    private Channel channel;
    private static ConnectionFactory connectionFactory;
    private static String loginQueueName;
    private static final String RPC_QUEUE_NAME = "rpc_regist_queue";
    private String result;
    static {
        Properties properties = new Properties();
        try {
            properties.load(Server.class.getClassLoader().getResourceAsStream("rabbitmq-talkroom.properties"));
            connectionFactory = new ConnectionFactory();
            connectionFactory.setHost(properties.getProperty("rabbitmq.host"));
            connectionFactory.setPort(Integer.valueOf(properties.getProperty("rabbitmq.port")));
            connectionFactory.setUsername(properties.getProperty("rabbitmq.username"));
            connectionFactory.setPassword(properties.getProperty("rabbitmq.password"));
            connectionFactory.setVirtualHost(properties.getProperty("rabbitmq.virtualHost"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void call(TalkUser talkUser, CountDownLatch countDownLatch) throws IOException, TimeoutException {
        connection = connectionFactory.newConnection();
        channel = connection.createChannel();
        //生成一个临时队列的名字
        loginQueueName = channel.queueDeclare().getQueue();
        //生成一个唯一的字符串
        final String corrId = UUID.randomUUID().toString();
        //将corrId、replyQueueName打包发送给consumer
        AMQP.BasicProperties props = new AMQP.BasicProperties()
                .builder()
                .correlationId(corrId)
                .replyTo(loginQueueName)
                .build();
        channel.basicPublish("", RPC_QUEUE_NAME, props, JSON.toJSONString(talkUser).getBytes());
        channel.basicConsume(loginQueueName, true, new DefaultConsumer(channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                super.handleDelivery(consumerTag, envelope, properties, body);
                //获取corrId相同的消息
                if(properties.getCorrelationId().equals(corrId)){
                    setResult(new String(body, "utf-8"));
                    countDownLatch.countDown();
                    try {
                        countDownLatch.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                   }
                }
            }
        });
        return;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
    public void close() throws IOException, TimeoutException {
        if(connection != null){
            channel.close();
            connection.close();
        }
    }
}
