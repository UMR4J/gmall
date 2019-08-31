package com.atguigu.gmall.payment.service.impl;

import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.bean.enums.PaymentStatus;
import com.atguigu.gmall.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

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
}
