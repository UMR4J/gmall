package com.atguigu.gmall.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.CartInfo;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.cart.constant.CartConst;
import com.atguigu.gmall.cart.mapper.CartInfoMapper;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.service.CartService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

/**
 * @author zdy
 * @create 2019-08-28 23:08
 */
@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartInfoMapper cartInfoMapper;
    @Reference
    private ManageService manageService;

    @Autowired
    RedisUtil redisUtil;

    @Override
    public void addToCart(String skuId, String userId, Integer skuNum) {

        CartInfo cartInfo = new CartInfo();
        cartInfo.setSkuId(skuId);
        cartInfo.setUserId(userId);
        CartInfo cartInfoExist = cartInfoMapper.selectOne(cartInfo);

        if (cartInfoExist != null) {

            cartInfoExist.setSkuNum(cartInfoExist.getSkuNum() + skuNum);
            // 给实时价格赋值
            cartInfoExist.setSkuPrice(cartInfoExist.getCartPrice());
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoExist);

        } else {
            SkuInfo skuInfo = manageService.getSkuInfoBySkuId(skuId);
            cartInfo.setSkuNum(skuNum);
            //cartInfo.setUserId(userId);
            //cartInfo.setSkuId(skuId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfoMapper.insertSelective(cartInfo);
            cartInfoExist = cartInfo;

        }

        Jedis jedis = null;

        try {
            jedis = redisUtil.getJedis();
            String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
            jedis.hset(cartKey, skuId, JSON.toJSONString(cartInfoExist));
            String userKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USERINFOKEY_SUFFIX;
            Long ttl = jedis.ttl(userKey);
            jedis.expire(cartKey, ttl.intValue());

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }


    }

    @Override
    public List<CartInfo> getCartList(String userId) {

        List<CartInfo> cartInfoList = new ArrayList<>();
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
            List<String> cartJsonList = jedis.hvals(cartKey);
            if (cartJsonList != null && cartJsonList.size() > 0) {
                for (String cartJson : cartJsonList) {

                    CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
                    cartInfoList.add(cartInfo);
                }

                cartInfoList.sort(new Comparator<CartInfo>() {
                    @Override
                    public int compare(CartInfo o1, CartInfo o2) {
                        return o1.getId().compareTo(o2.getId());
                    }
                });

                return cartInfoList;
            }


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

        cartInfoList = loadCartCache(userId);


        return cartInfoList;
    }

    @Override
    public List<CartInfo> mergeToCartList(List<CartInfo> cartListCookie, String userId) {

        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithCurPrice(userId);

        for (CartInfo cartInfoCookie : cartListCookie) {

            boolean flag = false;
            for (CartInfo cartInfo : cartInfoList) {

                if (cartInfoCookie.getSkuId().equals(cartInfo.getSkuId())) {
                    cartInfo.setSkuNum(cartInfo.getSkuNum() + cartInfoCookie.getSkuNum());
                    cartInfoMapper.updateByPrimaryKeySelective(cartInfo);
                    flag = true;
                }

            }
            if (!flag) {
                cartInfoCookie.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfoCookie);
            }

        }

        return loadCartCache(userId);
    }

    private List<CartInfo> loadCartCache(String userId) {
        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithCurPrice(userId);
        if (cartInfoList == null || cartInfoList.size() == 0) {
            return null;
        }

        Jedis jedis = null;

        try {
            jedis = redisUtil.getJedis();
            String cartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
            Map<String, String> map = new HashMap<>(cartInfoList.size());
            for (CartInfo cartInfo : cartInfoList) {
                String cartInfoJson = JSON.toJSONString(cartInfo);
                map.put(cartInfo.getSkuId(), cartInfoJson);
            }

            jedis.hmset(cartKey, map);

            //自己设置的过期时间课件上没有
            String userKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USERINFOKEY_SUFFIX;
            Long ttl = jedis.ttl(userKey);
            jedis.expire(cartKey, ttl.intValue());


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }

        return cartInfoList;
    }
}
