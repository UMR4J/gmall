package com.atguigu.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.*;
import com.atguigu.gmall.manage.mapper.*;
import com.atguigu.gmall.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

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
        BaseAttrInfo baseAttrInfo=new BaseAttrInfo();
        baseAttrInfo.setCatalog3Id(catalog3Id);
        return baseAttrInfoMapper.select(baseAttrInfo);
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

        //TODO transaction

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
}
