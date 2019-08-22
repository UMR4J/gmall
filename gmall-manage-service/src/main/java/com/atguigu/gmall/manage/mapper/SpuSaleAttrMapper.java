package com.atguigu.gmall.manage.mapper;

import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SpuSaleAttr;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author zdy
 * @create 2019-08-19 22:37
 */
public interface SpuSaleAttrMapper extends Mapper<SpuSaleAttr> {
    List<SpuSaleAttr> getSpuSaleAttrListBySpuId(String spuId);

    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(SkuInfo skuInfo);
}
