package com.atguigu.gmall.order.task;

import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author zdy
 * @create 2019-09-03 15:41
 */
@EnableScheduling
@Component
public class OrderTask {

    @Autowired
    private OrderService orderService;

    @Scheduled(cron = "0/20 * * * * 3")
    public  void checkOrder(){
        System.out.println("开始处理过期订单");

        long startTime = System.currentTimeMillis();

        List<OrderInfo> expiredOrderList = orderService.getExpiredOrderList();

        for (OrderInfo orderInfo : expiredOrderList) {
            orderService.execExpiredOrder(orderInfo);
        }
        long endTime = System.currentTimeMillis();

        System.out.println("一共处理"+expiredOrderList.size()+"个订单 共消耗"+(endTime-startTime)+"毫秒");


    }

    /*@Scheduled(cron = "5 * * * * ?")
    public void  work(){
        System.out.println("Thread ====== "+ Thread.currentThread());
    }

    @Scheduled(cron = "0/5 * * * * 1")
    public void  work1(){
        System.out.println("Thread0/5 ====== "+ Thread.currentThread());
    }

    @Scheduled(cron = "0/5 * * * * 2")
    public void  work2(){
        System.out.println("Thread周二 ====== "+ Thread.currentThread());
    }*/





}
