package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.manage.constant.ManageConst;
import com.atguigu.gmall.manage.mapper.*;
import com.atguigu.gmall.service.ManageService;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author zdy
 * @create 2019-08-16 21:07
 */
@Service
public class ManageServiceImpl implements ManageService {
    @Autowired
    private BaseCatalog1Mapper baseCatalog1Mapper;
    @Autowired
    private BaseCatalog2Mapper baseCatalog2Mapper;
    @Autowired
    private BaseCatalog3Mapper baseCatalog3Mapper;
    @Autowired
    private BaseAttrInfoMapper baseAttrInfoMapper;
    @Autowired
    private BaseAttrValueMapper baseAttrValueMapper;
    @Autowired
    private SpuInfoMapper spuInfoMapper;
    @Autowired
    private BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    private SpuImageMapper spuImageMapper;
    @Autowired
    private SpuSaleAttrMapper spuSaleAttrMapper;
    @Autowired
    private SpuSaleAttrValueMapper spuSaleAttrValueMapper;
    @Autowired
    private SkuInfoMapper skuInfoMapper;
    @Autowired
    private SkuImageMapper skuImageMapper;
    @Autowired
    private SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    private SkuSaleAttrValueMapper skuSaleAttrValueMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List<BaseCatalog1> getCatalog1() {
        return baseCatalog1Mapper.selectAll();
    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        Example example=new Example(BaseCatalog2.class);
        example.createCriteria().andEqualTo("catalog1Id", catalog1Id);
        return baseCatalog2Mapper.selectByExample(example);
    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
//        Example example=new Example(BaseCatalog3.class);
//        example.createCriteria().andEqualTo("catalog2Id", catalog2Id);
//        return baseCatalog3Mapper.selectByExample(example);
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        return baseCatalog3Mapper.select(baseCatalog3);
    }

    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {

//        Example example=new Example(BaseAttrInfo.class);
//        example.createCriteria().andEqualTo("catalog3Id", catalog3Id);
//        return baseAttrInfoMapper.selectByExample(example);

//        BaseAttrInfo baseAttrInfo=new BaseAttrInfo();
//        baseAttrInfo.setCatalog3Id(catalog3Id);
//        return baseAttrInfoMapper.select(baseAttrInfo);

        List<BaseAttrInfo> baseAttrInfoList = baseAttrInfoMapper.getBaseAttrInfoListByCatalog3Id(catalog3Id);

        return baseAttrInfoList;

    }

    @Transactional
    @Override
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {

        String baseAttrInfoId = baseAttrInfo.getId();
        if(baseAttrInfoId!=null&&baseAttrInfoId.length()>0){
            baseAttrInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);
        }else {
            baseAttrInfoMapper.insertSelective(baseAttrInfo);

        }
        //int i=10/0;
        BaseAttrValue bav = new BaseAttrValue();
        bav.setAttrId(baseAttrInfo.getId());

        baseAttrValueMapper.delete(bav);

        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();

        for (BaseAttrValue baseAttrValue : attrValueList) {
            baseAttrValue.setAttrId(baseAttrInfo.getId());
            baseAttrValueMapper.insertSelective(baseAttrValue);
        }


    }

    @Override
    public BaseAttrInfo getBaseAttrInfoById(BaseAttrValue baseAttrValue) {

        List<BaseAttrValue> attrValueList = baseAttrValueMapper.select(baseAttrValue);
        BaseAttrInfo baseAttrInfo=new BaseAttrInfo();
        baseAttrInfo.setAttrValueList(attrValueList);
        return baseAttrInfo;
    }

    @Override
    public List<SpuInfo> getSpuInfoListByCatalog3Id(SpuInfo spuInfo) {
        return spuInfoMapper.select(spuInfo);
    }

    @Override
    public List<BaseSaleAttr> getBaseSaleAttrList() {
        return baseSaleAttrMapper.selectAll();
    }

    @Transactional
    @Override
    public void saveSpuInfo(SpuInfo spuInfo) {

        if(spuInfo.getId()==null || spuInfo.getId().length()==0){
            spuInfo.setId(null);
            spuInfoMapper.insertSelective(spuInfo);
        }else {
            spuInfoMapper.updateByPrimaryKeySelective(spuInfo);
        }

        //delete SpuImage SpuSaleAttr SpuSaleAttrValue
        SpuImage spuImageDel=new SpuImage();
        spuImageDel.setSpuId(spuInfo.getId());
        spuImageMapper.delete(spuImageDel);

        SpuSaleAttr spuSaleAttrDel=new SpuSaleAttr();
        spuSaleAttrDel.setSpuId(spuInfo.getId());
        spuSaleAttrMapper.delete(spuSaleAttrDel);

        SpuSaleAttrValue spuSaleAttrValueDel=new SpuSaleAttrValue();
        spuSaleAttrValueDel.setSpuId(spuInfo.getId());
        spuSaleAttrValueMapper.delete(spuSaleAttrValueDel);



        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if(spuImageList!=null && spuImageList.size()>0){
            for (SpuImage spuImage : spuImageList) {
                spuImage.setId(null);
                spuImage.setSpuId(spuInfo.getId());
                spuImageMapper.insertSelective(spuImage);
            }

            List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
            if(spuSaleAttrList!=null && spuSaleAttrList.size()>0){
                for (SpuSaleAttr spuSaleAttr : spuSaleAttrList) {
                    spuSaleAttr.setId(null);
                    spuSaleAttr.setSpuId(spuInfo.getId());
                    spuSaleAttrMapper.insertSelective(spuSaleAttr);

                    List<SpuSaleAttrValue> spuSaleAttrValueList = spuSaleAttr.getSpuSaleAttrValueList();

                    for (SpuSaleAttrValue spuSaleAttrValue : spuSaleAttrValueList) {
                        spuSaleAttrValue.setSpuId(spuInfo.getId());
                        spuSaleAttrValueMapper.insertSelective(spuSaleAttrValue);
                    }


                }

            }

        }

    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListBySpuId(String spuId) {
        return spuSaleAttrMapper.getSpuSaleAttrListBySpuId(spuId);
    }

    @Override
    public List<SpuImage> getSpuImageListBySpuId(SpuImage spuImage) {

        return spuImageMapper.select(spuImage);
    }

    @Transactional
    @Override
    public void saveSkuInfo(SkuInfo skuInfo) {

        skuInfoMapper.insertSelective(skuInfo);
        String skuId = skuInfo.getId();

        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (skuImageList!=null && skuImageList.size()>0) {
            for (SkuImage skuImage : skuImageList) {
                skuImage.setSkuId(skuId);
                skuImageMapper.insertSelective(skuImage);
            }
        }

        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
        if (skuAttrValueList!=null && skuAttrValueList.size()>0) {
            for (SkuAttrValue skuAttrValue : skuAttrValueList) {
                skuAttrValue.setSkuId(skuId);
                skuAttrValueMapper.insertSelective(skuAttrValue);
            }
        }

        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (skuSaleAttrValueList!=null && skuSaleAttrValueList.size()>0) {
            for (SkuSaleAttrValue skuSaleAttrValue : skuSaleAttrValueList) {
                skuSaleAttrValue.setSkuId(skuId);
                skuSaleAttrValueMapper.insertSelective(skuSaleAttrValue);
            }
        }


    }

    @Override
    public SkuInfo getSkuInfoBySkuId(String skuId) {

        SkuInfo skuInfo=null;
        Jedis jedis=null;
        RLock lock=null;

        try {

            jedis=redisUtil.getJedis();

            String skuInfoKey= ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX;
            if (jedis.exists(skuInfoKey)){
                String skuInfoJson=jedis.get(skuInfoKey);
                if(!StringUtils.isEmpty(skuInfoJson)){
                    skuInfo = JSON.parseObject(skuInfoJson, SkuInfo.class);
                    System.out.println("缓存命中");
                    return skuInfo;
                }
            }else {
                Config config = new Config();
                config.useSingleServer().setAddress("redis://192.168.232.136:6379");
                RedissonClient redissonClient = Redisson.create(config);
                lock=redissonClient.getLock("myLock");
                lock.lock(10, TimeUnit.SECONDS);

                skuInfo=getSkuInfoDB(skuId);
                String toJSONString = JSON.toJSONString(skuInfo);
                jedis.setex(skuInfoKey, ManageConst.SKUKEY_TIMEOUT, toJSONString);
                return skuInfo;
            }


        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(jedis!=null){
                jedis.close();
            }
            if(lock!=null){
                lock.unlock();
            }
        }


        return getSkuInfoDB(skuId);

    }

    private SkuInfo getSkuInfoRedisson(String skuId) {
        SkuInfo skuInfo=null;
        Jedis jedis=null;
        RLock lock=null;

        try {
            Config config = new Config();
            config.useSingleServer().setAddress("redis://192.168.232.136:6379");
            RedissonClient redissonClient = Redisson.create(config);
            lock=redissonClient.getLock("myLock");
            lock.lock(10, TimeUnit.SECONDS);
            jedis=redisUtil.getJedis();

            String skuInfoKey= ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX;
            if (jedis.exists(skuInfoKey)){
                String skuInfoJson=jedis.get(skuInfoKey);
                if(!StringUtils.isEmpty(skuInfoJson)){
                    skuInfo = JSON.parseObject(skuInfoJson, SkuInfo.class);
                    System.out.println("缓存命中");
                    return skuInfo;
                }
            }else {
                skuInfo=getSkuInfoDB(skuId);
                String toJSONString = JSON.toJSONString(skuInfo);
                jedis.setex(skuInfoKey, ManageConst.SKUKEY_TIMEOUT, toJSONString);
                return skuInfo;
            }


        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(jedis!=null){
                jedis.close();
            }
            if(lock!=null){
                lock.unlock();
            }
        }


        return getSkuInfoDB(skuId);
    }


    //set分布式锁
    private SkuInfo getSkuInfo2(String skuId) {
        SkuInfo skuInfo=null;
        Jedis jedis=null;

        try {
            jedis = redisUtil.getJedis();
            String skuInfoKey= ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX;
            String skuInfoJson = jedis.get(skuInfoKey);
            if(skuInfoJson==null ||skuInfoJson.length()==0){
                System.out.println("没有命中缓存");
                String skuInfoLock=ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKULOCK_SUFFIX;
                String res = jedis.set(skuInfoKey, "OK", "NX", "PX", ManageConst.SKULOCK_EXPIRE_PX);
                if("OK".equals(res)){
                    System.out.println("获取锁");
                    skuInfo=getSkuInfoDB(skuId);
                    String toJSONString = JSON.toJSONString(skuInfo);
                    jedis.setex(skuInfoKey, ManageConst.SKUKEY_TIMEOUT, toJSONString);
                    return skuInfo;
                }else {
                    System.out.println("等待");
                    Thread.sleep(1000);
                    return getSkuInfoBySkuId(skuId);
                }

            }else {
                skuInfo = JSON.parseObject(skuInfoJson, SkuInfo.class);
                System.out.println("缓存命中");
                return skuInfo;
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(jedis!=null){
                jedis.close();
            }
        }


        return getSkuInfoDB(skuId);
    }

    //没有考虑缓存击穿问题
    private SkuInfo getSkuInfo1(String skuId) {
        SkuInfo skuInfo=null;
        Jedis jedis=null;
        try {
            jedis = redisUtil.getJedis();
            String skuInfoKey= ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX;
            if (jedis.exists(skuInfoKey)){
                String skuInfoJson = jedis.get(skuInfoKey);
                if(skuInfoJson!=null && skuInfoJson.length()>0){
                    skuInfo = JSON.parseObject(skuInfoJson, SkuInfo.class);
                    System.out.println("缓存命中");
                    return skuInfo;
                }
            }else {
                skuInfo=getSkuInfoDB(skuId);
                String toJSONString = JSON.toJSONString(skuInfo);
                jedis.setex(skuInfoKey, ManageConst.SKUKEY_TIMEOUT, toJSONString);
                return skuInfo;
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(jedis!=null){
                jedis.close();
            }
        }


        return getSkuInfoDB(skuId);
    }

    private SkuInfo getSkuInfoDB(String skuId) {
        SkuInfo skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        List<SkuImage> skuImageList = skuImageMapper.select(skuImage);
        skuInfo.setSkuImageList(skuImageList);

        //获取skuAttrValue全文检索用，保存到es中
        SkuAttrValue skuAttrValue=new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.select(skuAttrValue);
        skuInfo.setSkuAttrValueList(skuAttrValueList);

        return skuInfo;
    }


    @Override
    public List<SpuSaleAttr> selectSpuSaleAttrListCheckBySku(SkuInfo skuInfo) {
        return spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuInfo);
    }

    @Override
    public String getKeyIsSpuSaleAttrValueAndValueIsSkuIdJSON(SkuInfo skuInfo) {
        List<SkuSaleAttrValue> skuSaleAttrValueList= skuSaleAttrValueMapper.getSkuSaleAttrValueListBySpuId(skuInfo);

        Map<String,String> map=new HashMap<>();
        String valueKey="";
        for (int i = 0; i < skuSaleAttrValueList.size(); i++) {

            SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueList.get(i);
            if(valueKey.length()>0){
                valueKey+="|";
            }
            valueKey+=skuSaleAttrValue.getSaleAttrValueId();


            if(i==skuSaleAttrValueList.size()-1||!skuSaleAttrValue.getSkuId().equals(skuSaleAttrValueList.get(i + 1).getSkuId())){
                map.put(valueKey, skuSaleAttrValue.getSkuId());
                valueKey="";
            }


        }

        String valuesSkuJson = JSON.toJSONString(map);

        return valuesSkuJson;
    }

    @Override
    public List<BaseAttrInfo> getAttrList(List<String> attrValueIdList) {

        String attrValueIdStr = org.apache.commons.lang3.StringUtils.join(attrValueIdList.toArray(), ",");
        System.out.println("attrValueIdStr="+attrValueIdStr);
        return baseAttrInfoMapper.getBaseAttrInfoListByattrValueIdStr(attrValueIdStr);
    }
}
