package com.atguigu.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.cart.handler.CartCookieHandler;
import com.atguigu.gmall.config.LoginRequie;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManageService;
import lombok.extern.log4j.Log4j;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author zdy
 * @create 2019-08-28 21:02
 */
@Controller
public class CartController {

    @Reference
    private CartService cartService;

    @Reference
    private ManageService manageService;

    @Autowired
    private CartCookieHandler cartCookieHandler;

    @RequestMapping("cartList")
    @LoginRequie(autoRedirect = false)
    public  String cartList(HttpServletRequest request,HttpServletResponse response){

        String userId = (String) request.getAttribute("userId");

        List<CartInfo> cartList=null;

        if(userId!=null){

            List<CartInfo> cartListCookie=cartCookieHandler.getCartList(request);

            if(cartListCookie!=null&&cartListCookie.size()>0){

                cartList=cartService.mergeToCartList(cartListCookie,userId);
                cartCookieHandler.deleteCartCookie(request,response);

            }else {
                cartList = cartService.getCartList(userId);
            }

        }else {
            cartList = cartCookieHandler.getCartList(request);
        }

        request.setAttribute("cartList", cartList);

        return "cartList";

    }


        @LoginRequie(autoRedirect=false)
    @RequestMapping("addToCart")
    public String addToCart(HttpServletRequest request, HttpServletResponse response){

        String skuId = request.getParameter("skuId");
        String skuNum = request.getParameter("skuNum");



        String userId = (String) request.getAttribute("userId");

        if(userId!=null){
            cartService.addToCart(skuId, userId, Integer.parseInt(skuNum));
        }else {
            // 说明用户没有登录没有登录放到cookie中
            cartCookieHandler.addToCart(request, response, skuId, userId, Integer.parseInt(skuNum));
        }

        SkuInfo skuInfo = manageService.getSkuInfoBySkuId(skuId);
        request.setAttribute("skuInfo",skuInfo);
        request.setAttribute("skuNum",skuNum);


        return "success";
    }


}
