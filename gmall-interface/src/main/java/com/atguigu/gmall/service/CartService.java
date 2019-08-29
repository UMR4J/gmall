package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.CartInfo;

import java.util.List;

/**
 * @author zdy
 * @create 2019-08-28 23:06
 */
public interface CartService {
    public  void  addToCart(String skuId,String userId,Integer skuNum);

    List<CartInfo> getCartList(String userId);

    List<CartInfo> mergeToCartList(List<CartInfo> cartListCookie, String userId);
}
