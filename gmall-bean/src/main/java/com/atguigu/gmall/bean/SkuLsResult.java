package com.atguigu.gmall.bean;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author zdy
 * @create 2019-08-25 10:19
 */
@Data
public class SkuLsResult implements Serializable {

    List<SkuLsInfo> skuLsInfoList;

    long total;

    long totalPages;

    List<String> attrValueIdList;

}
