package com.atguigu.gmall.bean;

import lombok.Data;

import java.io.Serializable;

/**
 * @author zdy
 * @create 2019-08-25 10:17
 */
@Data
public class SkuLsParams implements Serializable {
    String  keyword;

    String catalog3Id;

    String[] valueId;

    int pageNo=1;

    int pageSize=20;

}
