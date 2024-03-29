package com.atguigu.gmall.service;

import com.atguigu.gmall.bean.*;

import java.util.List;

/**
 * @author zdy
 * @create 2019-08-16 21:06
 */
public interface ManageService {

    public List<BaseCatalog1> getCatalog1();

    public List<BaseCatalog2> getCatalog2(String catalog1Id);

    public List<BaseCatalog3> getCatalog3(String catalog2Id);

    public List<BaseAttrInfo> getAttrList(String catalog3Id);


    void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    BaseAttrInfo getBaseAttrInfoById(BaseAttrValue baseAttrValue);

    List<SpuInfo> getSpuInfoListByCatalog3Id(SpuInfo spuInfo);

    List<BaseSaleAttr> getBaseSaleAttrList();

    void saveSpuInfo(SpuInfo spuInfo);

    List<SpuSaleAttr> getSpuSaleAttrListBySpuId(String spuId);

    List<SpuImage> getSpuImageListBySpuId(SpuImage spuImage);

    void saveSkuInfo(SkuInfo skuInfo);

    SkuInfo getSkuInfoBySkuId(String skuId);

    List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(SkuInfo skuInfo);

    String getKeyIsSpuSaleAttrValueAndValueIsSkuIdJSON(SkuInfo skuInfo);

    List<BaseAttrInfo> getAttrList(List<String> attrValueIdList);
}
