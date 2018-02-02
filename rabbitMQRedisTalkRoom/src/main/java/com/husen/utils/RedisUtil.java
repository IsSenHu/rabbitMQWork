package com.husen.utils;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Properties;

/**
 * @author 11785
 */
public class RedisUtil {
    public static JedisPool jedisPool;

    /*
    * 根据配置文件得到参数，初始化连接池
    * */
    static{
        try{
            Properties properties = new Properties();
            properties.load(RedisUtil.class.getResourceAsStream("/redis-config.properties"));
            JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
            jedisPoolConfig.setMaxTotal(Integer.valueOf(properties.getProperty("redis.maxTotal")));
            jedisPoolConfig.setMaxIdle(Integer.valueOf(properties.getProperty("redis.maxIdle")));
            jedisPoolConfig.setMaxWaitMillis(Long.valueOf(properties.getProperty("redis.maxWaitMillis")));
            jedisPoolConfig.setTestOnBorrow(Boolean.valueOf(properties.getProperty("redis.testOnBorrow")));
            jedisPoolConfig.setTestOnReturn(Boolean.valueOf(properties.getProperty("redis.testOnReturn")));
            jedisPoolConfig.setBlockWhenExhausted(Boolean.valueOf(properties.getProperty("redis.blockWhenExhausted")));
            jedisPool = new JedisPool(jedisPoolConfig,
                    properties.getProperty("redis.host"),
                    Integer.valueOf(properties.getProperty("redis.port")),
                    Integer.valueOf(properties.getProperty("redis.timeout"))
                    );
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    /**
    * 获取jedis实例
    */
    public synchronized static Jedis getJedis(){
        try {
            if(jedisPool != null){
                Jedis jedis = jedisPool.getResource();
                return jedis;
            }else {
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
            return  null;
        }
    }

    /**
     * 释放jedis资源
     * @param jedis
     */
    public static void returnResource(final Jedis jedis){
        if(jedis != null){
            //高版本的jedisPool没有returnResource的方法，用jedis.close()取代
            jedis.close();
        }
    }
}
