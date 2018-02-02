package com.husen.rabbitmq;


import com.husen.pojo.TalkUser;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeoutException;

/**
 * @author 11785
 * 1.这是聊天的客户端
 * 2.其中的用户数据，聊天消息以及回调都是通过rabbitmq完成的
 * 3.使用分发模式来发送消息，可以让聊天室的人都收到
 * 4.使用rabbitmq的RPC来完成注册与登录，其中持久层用的hibernate框架
 */
public class Client{
    private Receiver receiver = new Receiver();
    private String nickName;
    private String password;
    private TalkUser talkUser = new TalkUser();
    private String registResult;
    private String loginResult;
    private Scanner scanner = new Scanner(System.in);
    private static final String NO = "no";
    private static final String FAILE = "faile";
    private static final String YES = "yes";
    private static final String EXIT = "exit";
    public boolean regist(){
        RegistServer registServer = null;
        RegistConsumer registConsumer = null;
        System.out.println("(๑•ᴗ•๑)请输入你的昵称");
        nickName = scanner.next();
        System.out.println("(๑•ᴗ•๑)请输入你的密码");
        password = scanner.next();
        talkUser.setNickName(nickName);
        talkUser.setPassword(password);
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try{
            System.out.println("ヘ(_ _ヘ)正在检查昵称是否存在...");
            registServer = new RegistServer();
            registConsumer = new RegistConsumer();
            registConsumer.regist();
            registServer.call(talkUser, countDownLatch);
            registServer.setResult(NO);
            countDownLatch.await();
            registResult = registServer.getResult();
            if (NO.equals(registResult)){
                System.out.println("ε(┬┬﹏┬┬)3该昵称已存在，请重新注册！");
                return false;
            } else if (FAILE.equals(registResult)){
                System.out.println("ε(┬┬﹏┬┬)3注册失败，请重新输入！");
                return false;
            }else {
                registConsumer.close();
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally {
            countDownLatch.countDown();
        }
    }
    public boolean login(){
        LoginServer loginServer = null;
        LoginConsumer loginConsumer = null;
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try{
            System.out.println("(๑•ᴗ•๑)请输入你的昵称");
            nickName = scanner.next();
            System.out.println("(๑•ᴗ•๑)请输入你的密码");
            password = scanner.next();
            talkUser.setNickName(nickName);
            talkUser.setPassword(password);
            System.out.println("(๑•ᴗ•๑)登陆中......");
            loginServer = new LoginServer();
            loginConsumer = new LoginConsumer();
            loginConsumer.login();
            loginServer.call(talkUser, countDownLatch);
            loginServer.setResult("no");
            countDownLatch.await();
            loginResult = loginServer.getResult();
            if(NO.equals(loginResult)){
                System.out.println("ε(┬┬﹏┬┬)3用户名不存在或密码错误！");
                return false;
            }else {
                loginConsumer.close();
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally {
            countDownLatch.countDown();
        }
    }
    private void startTalk() throws IOException, TimeoutException, InterruptedException {
        System.out.println("o(*￣▽￣*)o欢迎来到RabbitMQ聊天世界");
        System.out.println("╰(￣▽￣)╮是否第一次登录？");
        start : while (true){
            System.out.println("==========================yes : 前往注册==========================");
            System.out.println("==========================no : 前往登录===========================");
            String isFirst = scanner.next();
            if(YES.equalsIgnoreCase(isFirst)){
                if(regist()){
                    System.out.println("o(*￣▽￣*)o恭喜你注册成功");
                    break start;
                }
            }else if (NO.equalsIgnoreCase(isFirst)){
                if (login()){
                    break start;
                }
            }else {
                System.out.println("======================请输入\"yes\" or \"no\"=====================");
            }
        }
        receiver.receive();
        System.out.println("o(≧v≦)o~~成功登录RabbitMQ聊天室~_~");
        System.out.println("==========================你的昵称为：" + nickName + "==========================");
        System.out.println("==========================输入你想说的话点击回车即可发送，或者输入exit退出聊天！==========================");
        while (true){
            String message = scanner.next();
            if(EXIT.equals(message)){
                break;
            }
            Server.sendMessage(nickName + "说：" + message);
        }
        receiver.closeReceiver();
        Server.closeTalkRoom();
        System.out.println("==========================你已退出==========================");
        System.out.println("( ^_^ )/~~拜拜");
        System.exit(0);
    }
    public static void main(String[] args) {
        try {
            Client client = new Client();
            client.startTalk();
        }catch (IOException e){
            System.out.println("IO流异常");
            System.exit(0);
        }catch (TimeoutException e2){
            System.out.println("连接超时");
            System.exit(0);
        }catch (InterruptedException e3){
            System.out.println("打断异常");
            System.exit(0);
        }
    }

}
