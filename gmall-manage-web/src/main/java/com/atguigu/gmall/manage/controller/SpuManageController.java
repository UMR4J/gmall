package com.atguigu.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.BaseSaleAttr;
import com.atguigu.gmall.bean.SpuImage;
import com.atguigu.gmall.bean.SpuInfo;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.service.ManageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author zdy
 * @create 2019-08-19 16:41
 */
@RestController
@CrossOrigin
public class SpuManageController {

    @Reference
    private ManageService manageService;

    @RequestMapping("spuList")
    public List<SpuInfo> spuList(SpuInfo spuInfo){


        return manageService.getSpuInfoListByCatalog3Id(spuInfo);

    }

    @RequestMapping("baseSaleAttrList")
    public List<BaseSaleAttr> getBaseSaleAttrList(){
        return   manageService.getBaseSaleAttrList();
    }

    @RequestMapping("saveSpuInfo")
    public String saveSpuInfo(@RequestBody SpuInfo spuInfo){
        manageService.saveSpuInfo(spuInfo);
        return  "OK";
    }
    //spuSaleAttrList
    @RequestMapping("spuSaleAttrList")
    public List<SpuSaleAttr> getSpuSaleAttrListBySpuId(String spuId){
        return manageService.getSpuSaleAttrListBySpuId(spuId);
    }

    //spuImageList
    @RequestMapping("spuImageList")
    public List<SpuImage> getSpuImageListBySpuId(SpuImage spuImage){
        return manageService.getSpuImageListBySpuId(spuImage);
    }


}
