package com.atguigu.gmall.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.bean.enums.PaymentStatus;
import com.atguigu.gmall.config.ActiveMQUtil;
import com.atguigu.gmall.payment.config.WeiXinPayConfig;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.PaymentService;
import com.atguigu.gmall.util.HttpClient;
import com.github.wxpay.sdk.WXPayUtil;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.jms.Connection;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zdy
 * @create 2019-08-31 19:40
 */
@Service//不需要用dubbo中的@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PaymentInfoMapper paymentInfoMapper;
    @Autowired
    private AlipayClient alipayClient;
    @Reference
    private OrderService orderService;
    @Autowired
    private ActiveMQUtil activeMQUtil;

    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public PaymentInfo getPaymentInfo(String out_trade_no) {
        PaymentInfo paymentInfo=new PaymentInfo();
        paymentInfo.setOutTradeNo(out_trade_no);
        return paymentInfoMapper.selectOne(paymentInfo);
    }

    @Override
    public void updatePaymentInfo(String outTradeNo, Map<String, String> paramMap) {
        PaymentInfo paymentInfoUpd=new PaymentInfo();
        paymentInfoUpd.setPaymentStatus(PaymentStatus.PAID);
        paymentInfoUpd.setCallbackTime(new Date());
        paymentInfoUpd.setCallbackContent(paramMap.toString());

        Example example=new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo",outTradeNo);

        paymentInfoMapper.updateByExampleSelective(paymentInfoUpd, example);
    }

    @Override
    public boolean refund(String orderId) {

        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();

        PaymentInfo paymentInfo=new PaymentInfo();
        paymentInfo.setOrderId(orderId);
        PaymentInfo pi = paymentInfoMapper.selectOne(paymentInfo);

        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no",pi.getOutTradeNo());
        map.put("refund_amount", pi.getTotalAmount());

        request.setBizContent(JSON.toJSONString(map));
        AlipayTradeRefundResponse response = null;

        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if(response.isSuccess()){
            System.out.println("调用成功");
            return true;
        } else {
            System.out.println("调用失败");
            return false;
        }


    }

    @Override
    public Map createNative(String orderId) {

        OrderInfo orderInfo = orderService.getOrderInfo(orderId);

        Map<String,String> param=new HashMap();
        param.put("appid", WeiXinPayConfig.APPID);
        param.put("mch_id", WeiXinPayConfig.PARTNER);
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        param.put("body","还没想好");
        param.put("out_trade_no", orderInfo.getOutTradeNo());

        String totalFee=String.valueOf(orderInfo.getTotalAmount().multiply(new BigDecimal(100)).toBigInteger());
        param.put("total_fee",totalFee);//总金额（分）
        param.put("notify_url", "http://trade.gmall.com/trade");//回调地址
        param.put("trade_type", "NATIVE");//交易类型
        try {
            String xmlParam = WXPayUtil.generateSignedXml(param, WeiXinPayConfig.PARTNERKEY);
            System.out.println("the xmlParam is=======================");
            System.out.println(xmlParam);
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();
            //3.获得结果
            String result = client.getContent();
            System.out.println("client.getContent()获得结果-----------------------");
            System.out.println(result);
            Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
            Map<String, String> map=new HashMap<>();
            map.put("code_url", resultMap.get("code_url"));//支付地址
            map.put("total_fee", totalFee);//总金额
            map.put("out_trade_no",orderInfo.getOutTradeNo());//订单号
            return map;


        } catch (Exception e) {
            e.printStackTrace();
        }

        return new HashMap<>();
    }

    @Override
    public void sendPaymentResult(PaymentInfo paymentInfo, String result) {
        Connection connection = activeMQUtil.getConnection();
        try {

            connection.start();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue queue = session.createQueue("PAYMENT_RESULT_QUEUE");
            MessageProducer producer = session.createProducer(queue);

            ActiveMQMapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("orderId",paymentInfo.getOrderId());
            mapMessage.setString("result",result);
            producer.send(mapMessage);
            session.commit();

            producer.close();
            session.close();
            connection.close();



        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
