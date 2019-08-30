package com.atguigu.gmall.cart.mapper;

import com.atguigu.gmall.bean.CartInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author zdy
 * @create 2019-08-28 23:11
 */
public interface CartInfoMapper extends Mapper<CartInfo> {
    List<CartInfo> selectCartListWithCurPrice(String userId);
}
