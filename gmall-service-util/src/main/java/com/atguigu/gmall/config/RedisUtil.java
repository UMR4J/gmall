package com.atguigu.gmall.config;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author zdy
 * @create 2019-08-23 16:20
 */
public class RedisUtil {

    private JedisPool jedisPool;

    public void initJedisPool(String host,int port,int timeOut,int database){

        JedisPoolConfig jedisPoolConfig=new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(200);
        jedisPoolConfig.setMaxWaitMillis(10*1000);
        jedisPoolConfig.setMinIdle(10);
        jedisPoolConfig.setBlockWhenExhausted(true);
        jedisPoolConfig.setTestOnBorrow(true);
        jedisPool=new JedisPool(jedisPoolConfig, host, port, timeOut);

    }

    public Jedis getJedis(){
        return jedisPool.getResource();
    }

}
