package com.atguigu.gmall.cart.handler;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.config.CookieUtil;
import com.atguigu.gmall.service.ManageService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zdy
 * @create 2019-08-28 23:49
 */
@Component
public class CartCookieHandler {

    // 定义购物车名称
    private String cookieCartName = "CART";
    // 设置cookie 过期时间
    private int COOKIE_CART_MAXAGE = 7 * 24 * 3600;

    @Reference
    private ManageService manageService;

    public void addToCart(HttpServletRequest request, HttpServletResponse response, String skuId, String userId, Integer skuNum) {

        String cartJson = CookieUtil.getCookieValue(request, cookieCartName, true);
        List<CartInfo> cartInfoList = new ArrayList<>();

        boolean isExist = false;

        if (cartJson != null) {
            cartInfoList = JSON.parseArray(cartJson, CartInfo.class);
            for (CartInfo cartInfo : cartInfoList) {

                if (cartInfo.getSkuId().equals(skuId)) {
                    cartInfo.setSkuNum(cartInfo.getSkuNum() + skuNum);
                    cartInfo.setSkuPrice(cartInfo.getCartPrice());
                    isExist = true;
                    break;
                }

            }
        }
        if (!isExist) {
            CartInfo cartInfo = new CartInfo();
            SkuInfo skuInfo = manageService.getSkuInfoBySkuId(skuId);
            cartInfo.setSkuNum(skuNum);
            cartInfo.setUserId(userId);
            cartInfo.setSkuId(skuId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfoList.add(cartInfo);
        }


        CookieUtil.setCookie(request, response, cookieCartName, JSON.toJSONString(cartInfoList), COOKIE_CART_MAXAGE, true);


    }


    public List<CartInfo> getCartList(HttpServletRequest request) {

        List<CartInfo> cartInfoList = new ArrayList<>();

        String cartInfoListJson = CookieUtil.getCookieValue(request, cookieCartName, true);
        if (!StringUtils.isEmpty(cartInfoListJson)) {
            cartInfoList = JSON.parseArray(cartInfoListJson, CartInfo.class);

        }


        return cartInfoList;

    }

    public void deleteCartCookie(HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.deleteCookie(request, response, cookieCartName);
    }

    public void checkCart(HttpServletRequest request, HttpServletResponse response, String skuId, String isChecked) {

        List<CartInfo> cartList = getCartList(request);

        for (CartInfo cartInfo : cartList) {
            if (cartInfo.getSkuId().equals(skuId)) {
                cartInfo.setIsChecked(isChecked);
            }
        }

        CookieUtil.setCookie(request, response, cookieCartName, JSON.toJSONString(cartList), COOKIE_CART_MAXAGE, true);


    }
}
