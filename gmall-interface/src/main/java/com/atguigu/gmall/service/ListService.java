package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParams;
import com.atguigu.gmall.bean.SkuLsResult;

/**
 * @author zdy
 * @create 2019-08-24 19:26
 */
public interface ListService {
    public void saveSkuLsInfo(SkuLsInfo skuLsInfo);

    public SkuLsResult search(SkuLsParams skuLsParams);

    public void incrHotScore(String skuId);
}
