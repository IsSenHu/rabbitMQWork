package com.husen.rabbitmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.husen.pojo.TalkUser;
import com.rabbitmq.client.*;

import java.io.IOException;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

/**
 * @author 11785
 */
public class RegistConsumer {
    private static Connection connection;
    private static Channel channel;
    private static String loginQueueName;
    private TalkDao talkDao = new TalkDao();
    private static final String RPC_QUEUE_NAME = "rpc_regist_queue";
    private static final String NO = "no";
    private static final String RESULT = "result";
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
            //生成一个临时队列的名字
            loginQueueName = channel.queueDeclare().getQueue();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public void regist() throws IOException, TimeoutException {
        try{
            channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);
            Consumer consumer = new DefaultConsumer(channel){
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    super.handleDelivery(consumerTag, envelope, properties, body);
                    AMQP.BasicProperties replyProps = new AMQP.BasicProperties()
                            .builder()
                            .correlationId(properties.getCorrelationId())
                            .build();
                    String response = "";
                    try{
                        String message = new String(body, "utf-8");
                        TalkUser talkUser = JSON.parseObject(message, new TypeReference<TalkUser>(){});
                        Map<String, Object> map = talkDao.exitsNickName(talkUser.getNickName());
                        if(NO.equals(map.get(RESULT))){
                            response = "no";
                        }else {
                            boolean success = talkDao.addTalkUser(talkUser);
                            if(success){
                                response = "yes";
                            }else {
                                response = "faile";
                            }

                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }finally {
                        channel.basicPublish("", properties.getReplyTo(), replyProps, response.getBytes());
                        channel.basicAck(envelope.getDeliveryTag(), false);
                    }
                }
            };
            channel.basicConsume(RPC_QUEUE_NAME, false, consumer);
        }catch (Exception e){
            channel.close();
            connection.close();
            e.printStackTrace();
        }
    }
    public void close() throws IOException, TimeoutException {
        channel.close();
        connection.close();
    }
}
