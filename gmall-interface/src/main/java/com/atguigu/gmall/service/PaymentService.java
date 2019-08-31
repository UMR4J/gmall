package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.PaymentInfo;

import java.util.Map;

/**
 * @author zdy
 * @create 2019-08-31 19:39
 */
public interface PaymentService {
    void savePaymentInfo(PaymentInfo paymentInfo);

    PaymentInfo getPaymentInfo(String out_trade_no);

    void updatePaymentInfo(String outTradeNo, Map<String, String> paramMap);

    boolean refund(String orderId);
}
