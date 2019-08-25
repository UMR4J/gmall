package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author zdy
 * @create 2019-08-20 19:46
 */
@RestController
@CrossOrigin
public class SkuManageController {

    @Reference
    private ManageService manageService;

    @Reference
    private ListService listService;

    //saveSkuInfo
    @RequestMapping("saveSkuInfo")
    public String saveSkuInfo(@RequestBody SkuInfo skuInfo) {

        if (skuInfo != null) {
            manageService.saveSkuInfo(skuInfo);
        }

        return "OK";
    }
    @RequestMapping("onSale")
    public String onSale(String skuId){

        SkuInfo skuInfo = manageService.getSkuInfoBySkuId(skuId);
        SkuLsInfo skuLsInfo=new SkuLsInfo();
        BeanUtils.copyProperties(skuInfo, skuLsInfo);
        listService.saveSkuLsInfo(skuLsInfo);
        return "ok";

    }


}
