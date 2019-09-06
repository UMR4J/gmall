package com.atguigu.gmall.payment.mq;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;

/**
 * @author zdy
 * @create 2019-09-03 14:25
 */
@Component
public class PaymentConsumer {

    @Autowired
    private PaymentService paymentService;

    @JmsListener(destination = "PAYMENT_RESULT_CHECK_QUEUE",containerFactory = "jmsQueueListener")
    public void consumeSkuDeduct(MapMessage mapMessage) throws JMSException {
        // 获取消息队列中的数据
        String outTradeNo = mapMessage.getString("outTradeNo");
        int delaySec = mapMessage.getInt("delaySec");
        int checkCount = mapMessage.getInt("checkCount");


        boolean result = paymentService.checkPayment(outTradeNo);
        System.out.println("检查支付结果："+result);

        if(!result&&checkCount>0){
            paymentService.sendDelayPaymentResult(outTradeNo, delaySec, checkCount-1);
            System.out.println("checkCount="+checkCount);
        }

    }
}
