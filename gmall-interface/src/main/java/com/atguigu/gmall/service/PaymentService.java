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

    Map createNative(String orderId);

    void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUPD);

    void sendPaymentResult(PaymentInfo paymentInfo, String result);

    void sendDelayPaymentResult(String outTradeNo,int delaySec ,int checkCount);

    boolean checkPayment(String outTradeNo);

    PaymentInfo getPaymentInfo(PaymentInfo paymentInfoQuery);

    void closePayment(String id);
}
