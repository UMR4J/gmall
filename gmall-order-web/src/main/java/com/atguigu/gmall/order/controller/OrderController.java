package com.atguigu.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author zdy
 * @create 2019-08-16 18:24
 */
@Controller
public class OrderController {

    @Reference
    private UserService userService;

    @ResponseBody
    @RequestMapping("trade")
    public List<UserAddress> getUserAddressListByUserId(UserAddress userAddress){

        return userService.getUserAddressListByUserId(userAddress);

    }

}
