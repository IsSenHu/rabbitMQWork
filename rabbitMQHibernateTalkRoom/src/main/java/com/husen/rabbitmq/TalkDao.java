package com.husen.rabbitmq;

import com.husen.pojo.TalkUser;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author 11785
 */
public class TalkDao {
    private static Configuration configuration = new Configuration();
    private static SessionFactory sessionFactory;
    static {
        try {
            configuration.configure("hibernate.cfg.xml");
            sessionFactory = configuration.buildSessionFactory();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    public Map<String, Object> exitsNickName(String nickName){
        Session session = sessionFactory.getCurrentSession();
        //开启事务
        Transaction transaction = session.beginTransaction();
        try{
            TalkUser talkUser = session.get(TalkUser.class, nickName);
            Map<String, Object> map = new HashMap<>(2);
            if(talkUser == null){
                map.put("result", "ok");
            }else {
                map.put("result", "no");
            }
            transaction.commit();
            return map;
        }catch (Exception e){
            e.printStackTrace();
            transaction.rollback();
            return null;
        }
    }
    public boolean addTalkUser(TalkUser talkUser){
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        try{
            session.save(talkUser);
            transaction.commit();
            return true;
        }catch (Exception e){
            e.printStackTrace();
            transaction.rollback();
            return false;
        }
    }
    public void exit(){

    }

    public boolean login(TalkUser talkUser) {
        Session session = sessionFactory.getCurrentSession();
        Transaction transaction = session.beginTransaction();
        try{
            Query query = session.createQuery("from t_talk_user where nickName = ? and password = ?");
            query.setParameter(0, talkUser.getNickName());
            query.setParameter(1, talkUser.getPassword());
            List<TalkUser> talkUsers = query.list();
            if (talkUsers.size() == 0){
                transaction.commit();
                return false;
            }else {
                transaction.commit();
                return true;
            }
        }catch (Exception e){
            e.printStackTrace();
            transaction.rollback();
            return false;
        }
    }
}
