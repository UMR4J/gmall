package com.atguigu.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.PaymentInfo;
import com.atguigu.gmall.bean.enums.PaymentStatus;
import com.atguigu.gmall.config.LoginRequie;
import com.atguigu.gmall.payment.config.AlipayConfig;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zdy
 * @create 2019-08-31 18:12
 */
@Controller
public class PaymentController {

    @Reference
    private OrderService orderService;
    @Autowired//实现类就在当前模块中
    private PaymentService paymentService;

    @Autowired
    private AlipayClient alipayClient;

    @RequestMapping("index")
    @LoginRequie
    public String index(HttpServletRequest request, Model model) {

        String orderId = request.getParameter("orderId");
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        model.addAttribute("orderId", orderId);
        model.addAttribute("totalAmount", orderInfo.getTotalAmount());
        return "index";

    }

    @RequestMapping(value = "/alipay/submit", method = RequestMethod.POST)
    @ResponseBody
    public String submitPayment(HttpServletRequest request, HttpServletResponse response) {

        String orderId = request.getParameter("orderId");
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
        paymentInfo.setOrderId(orderId);
        paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
        paymentInfo.setSubject(orderInfo.getTradeBody());
        paymentInfo.setPaymentStatus(PaymentStatus.UNPAID);
        //save paymentInfo
        paymentService.savePaymentInfo(paymentInfo);

        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);

        Map<String, Object> bizContnetMap = new HashMap<>();
        bizContnetMap.put("out_trade_no", paymentInfo.getOutTradeNo());
        bizContnetMap.put("product_code", "FAST_INSTANT_TRADE_PAY");
        bizContnetMap.put("subject", paymentInfo.getSubject());//订单标题
        bizContnetMap.put("total_amount", paymentInfo.getTotalAmount());

        alipayRequest.setBizContent(JSON.toJSONString(bizContnetMap));

        String form = "";

        try {
            form = alipayClient.pageExecute(alipayRequest).getBody();
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        response.setContentType("text/html;charset=UTF-8");
        return form;

    }

    @RequestMapping(value = "/alipay/callback/return", method = RequestMethod.GET)
    public String callbackReturn() {
        return "redirect:" + AlipayConfig.return_order_url;
    }

    @RequestMapping(value = "/alipay/callback/notify", method = RequestMethod.POST)
    @ResponseBody
    public String paymentNotify(@RequestParam Map<String, String> paramMap, HttpServletRequest request) throws AlipayApiException {

        boolean flag = AlipaySignature.rsaCheckV1(paramMap, AlipayConfig.alipay_public_key, "utf-8", AlipayConfig.sign_type);
        if (!flag) {
            return "fail";
        }

        String trade_status = paramMap.get("trade_status");
        if ("TRADE_SUCCESS".equals(trade_status) || "TRADE_FINISHED".equals(trade_status)) {

            String outTradeNo = paramMap.get("out_trade_no");
            PaymentInfo paymentInfoHas = paymentService.getPaymentInfo(outTradeNo);
            if (paymentInfoHas.getPaymentStatus() == PaymentStatus.PAID || paymentInfoHas.getPaymentStatus() == PaymentStatus.ClOSED) {
                return "fail";
            } else {
                //更新paymentInfo状态
                paymentService.updatePaymentInfo(outTradeNo, paramMap);
                //sendPaymentResult(paymentInfoHas,"success");
                return "success";
            }

        }

        return "fail";
    }

    @RequestMapping("refund")
    @ResponseBody
    public String refund(String orderId) {
        boolean flag = paymentService.refund(orderId);
        System.out.println("flag:" + flag);
        return String.valueOf(flag);
    }

    @RequestMapping("wx/submit")
    @ResponseBody
    public Map createNative(String orderId) {

        Map map = paymentService.createNative(orderId);
        System.out.println(map.get("code_url"));
        return map;


    }

    // 发送验证,这是一个测试方法，正常应该写在支付后的异步回调方法中
    @RequestMapping("sendPaymentResult")
    @ResponseBody
    public String sendPaymentResult(PaymentInfo paymentInfo,@RequestParam("result") String result){
        paymentService.sendPaymentResult(paymentInfo,result);
        return "sent payment result";
    }

}