package com.husen.rabbitmq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.husen.pojo.TalkUser;
import com.husen.utils.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import redis.clients.jedis.Jedis;

/**
 * @author 11785
 */
public class TalkDao {
    private static final String NICKNAMW_TABLE = "t_nickname";

    /**
     * 判断该昵称存在不
     * @param nickName
     * @return
     */
    public boolean exitsNickName(String nickName){
        Jedis jedis = RedisUtil.getJedis();
        boolean isExits = jedis.hexists(NICKNAMW_TABLE, nickName);
        jedis.close();
        return isExits;
    }

    /**
     * 添加、注册聊天的用户
     * @param talkUser
     * @return
     */
    public boolean addTalkUser(TalkUser talkUser){
        Jedis jedis = RedisUtil.getJedis();
        long result = jedis.hset(NICKNAMW_TABLE, talkUser.getNickName(), JSON.toJSONString(talkUser));
        jedis.close();
        return result == 1 ? true : false;
    }

    /**
     * 登录
     * @param talkUser
     * @return
     */
    public boolean login(TalkUser talkUser) {
        Jedis jedis = RedisUtil.getJedis();
        String jsonStr = jedis.hget(NICKNAMW_TABLE, talkUser.getNickName());
        if(StringUtils.isEmpty(jsonStr)){
            return false;
        }
        TalkUser talkUserRedis = JSON.parseObject(jsonStr, new TypeReference<TalkUser>(){});
        if(StringUtils.equals(talkUser.getPassword(), talkUserRedis.getPassword())){
            return true;
        }else {
            return false;
        }
    }
}
