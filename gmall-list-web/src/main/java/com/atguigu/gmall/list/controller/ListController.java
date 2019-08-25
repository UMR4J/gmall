package com.atguigu.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.SkuLsParams;
import com.atguigu.gmall.bean.SkuLsResult;
import com.atguigu.gmall.service.ListService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zdy
 * @create 2019-08-25 12:06
 */
@RestController
public class ListController {

    @Reference
    private ListService listService;

    @RequestMapping("list.html")
    public String getList(SkuLsParams skuLsParams){

        SkuLsResult skuLsResult = listService.search(skuLsParams);

        String jsonString = JSON.toJSONString(skuLsResult);

        return jsonString;

    }
}
