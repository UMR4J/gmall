package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.util.HttpClientUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;

import java.util.*;

/**
 * @author zdy
 * @create 2019-08-30 21:10
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Transactional
    @Override
    public String saveOrder(OrderInfo orderInfo) {

        //订单的创建时间
        orderInfo.setCreateTime(new Date());
        //订单的失效时间
        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        orderInfo.setExpireTime(calendar.getTime());
        // 生成第三方支付编号
        String outTradeNo="ATGUIGU"+System.currentTimeMillis()+""+new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfoMapper.insertSelective(orderInfo);

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);
        }

        // 为了跳转到支付页面使用。支付会根据订单id进行支付。
        String orderId = orderInfo.getId();
        return orderId;

    }

    @Override
    public String getTradeNo(String userId) {

        Jedis jedis=null;
        try {

            jedis= redisUtil.getJedis();
            String tradeNoKey="user:"+userId+":tradeCode";
            String tradeCode = UUID.randomUUID().toString();
            jedis.setex(tradeNoKey,10*60,tradeCode);
            return tradeCode;

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(jedis!=null){
                jedis.close();
            }
        }
        return null;
    }

    @Override
    public boolean checkTradeCode(String userId, String tradeCodeNo) {

        Jedis jedis=null;
        try {

            jedis= redisUtil.getJedis();
            String tradeNoKey="user:"+userId+":tradeCode";
            String codeNo = jedis.get(tradeNoKey);
            if(tradeCodeNo!=null){
                return tradeCodeNo.equals(codeNo);
            }else {
                return false;
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(jedis!=null){
                jedis.close();
            }
        }
        return false;
    }

    @Override
    public void delTradeCode(String userId) {

        Jedis jedis=null;
        try {

            jedis= redisUtil.getJedis();
            String tradeNoKey="user:"+userId+":tradeCode";
            jedis.del(tradeNoKey);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(jedis!=null){
                jedis.close();
            }
        }

    }

    @Override
    public boolean checkStock(String skuId, Integer skuNum) {

        String result = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + skuId + "&num=" + skuNum);


        return "1".equals(result);
    }
}
