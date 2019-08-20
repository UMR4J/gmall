package com.atguigu.gmall.manage.mapper;

import com.atguigu.gmall.bean.BaseAttrInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * @author zdy
 * @create 2019-08-16 21:04
 */
public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {
    List<BaseAttrInfo> getBaseAttrInfoListByCatalog3Id(String catalog3Id);
}
