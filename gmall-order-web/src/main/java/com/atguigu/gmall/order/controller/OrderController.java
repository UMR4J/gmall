package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.enums.OrderStatus;
import com.atguigu.gmall.bean.enums.ProcessStatus;
import com.atguigu.gmall.config.LoginRequie;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zdy
 * @create 2019-08-16 18:24
 */
@Controller
public class OrderController {

    @Reference
    private UserService userService;
    @Reference
    private CartService cartService;
    @Reference
    private OrderService orderService;

    @RequestMapping(value = "trade")
    @LoginRequie
    public  String tradeInit(HttpServletRequest request){

        String userId = (String) request.getAttribute("userId");
        List<CartInfo> cartCheckedList = cartService.getCartCheckedList(userId);
        List<UserAddress> userAddressList = userService.getUserAddressList(userId);


        List<OrderDetail> orderDetailList=new ArrayList<>(cartCheckedList.size());
        for (CartInfo cartInfo : cartCheckedList) {
            OrderDetail orderDetail=new OrderDetail();

            orderDetail.setSkuId(cartInfo.getSkuId());
            orderDetail.setSkuName(cartInfo.getSkuName());
            orderDetail.setImgUrl(cartInfo.getImgUrl());
            orderDetail.setSkuNum(cartInfo.getSkuNum());
            orderDetail.setOrderPrice(cartInfo.getCartPrice());
            orderDetailList.add(orderDetail);

        }

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setOrderDetailList(orderDetailList);
        orderInfo.sumTotalAmount();


        request.setAttribute("orderDetailList",orderDetailList);
        request.setAttribute("totalAmount", orderInfo.getTotalAmount());
        request.setAttribute("userAddressList",userAddressList);
        request.setAttribute("tradeCode", orderService.getTradeNo(userId));

        return "trade";
    }

    @RequestMapping(value = "submitOrder",method = RequestMethod.POST)
    @LoginRequie
    public String submitOrder(OrderInfo orderInfo,HttpServletRequest request){

        String userId = (String) request.getAttribute("userId");
        String tradeNo = request.getParameter("tradeNo");
        boolean result = orderService.checkTradeCode(userId, tradeNo);
        if(!result){
            request.setAttribute("errMsg","该页面已失效，请重新结算!");
            return "tradeFail";
        }

        orderInfo.setUserId(userId);
        orderInfo.sumTotalAmount();
        orderInfo.setOrderStatus(OrderStatus.UNPAID);
        orderInfo.setProcessStatus(ProcessStatus.UNPAID);

        //校验库存
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            boolean res = orderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
            if(!res){
                request.setAttribute("errMsg","商品库存不足，请重新下单！");
                return "tradeFail";
            }
        }

        String orderId = orderService.saveOrder(orderInfo);

        //删除购物车中选中的商品
        cartService.deleteCartCheckedList(userId);

        orderService.delTradeCode(userId);
        return "redirect://payment.gmall.com/index?orderId="+orderId;

    }

    //拆单
    @RequestMapping("orderSplit")
    @ResponseBody
    public String orderSplit(HttpServletRequest request){

        String orderId = request.getParameter("orderId");
        String wareSkuMap = request.getParameter("wareSkuMap");

        List<OrderInfo> subOrderInfoList = orderService.splitOrder(orderId,wareSkuMap);

        List<Map> wareMapList=new ArrayList<>();
        for (OrderInfo orderInfo : subOrderInfoList) {
            Map map = orderService.initWareOrder(orderInfo);
            wareMapList.add(map);
        }
        return JSON.toJSONString(wareMapList);

    }




    /*@ResponseBody
    @RequestMapping("trade")
    public List<UserAddress> getUserAddressListByUserId(UserAddress userAddress){

        return userService.getUserAddressListByUserId(userAddress);

    }*/

}
