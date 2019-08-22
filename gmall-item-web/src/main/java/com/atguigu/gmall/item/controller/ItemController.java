package com.atguigu.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.SkuInfo;
import com.atguigu.gmall.bean.SpuSaleAttr;
import com.atguigu.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

/**
 * @author zdy
 * @create 2019-08-20 21:19
 */
@Controller
public class ItemController {

    @Reference
    private ManageService manageService;

    @RequestMapping("{skuId}.html")
    public String skuInfoPage(@PathVariable(value = "skuId") String skuId, Model model){

        SkuInfo skuInfo=manageService.getSkuInfoBySkuId(skuId);

        List<SpuSaleAttr> spuSaleAttrList=manageService.selectSpuSaleAttrListCheckBySku(skuInfo);

        String valuesSkuJson=manageService.getKeyIsSpuSaleAttrValueAndValueIsSkuIdJSON(skuInfo);
        model.addAttribute("skuInfo", skuInfo);
        model.addAttribute("spuSaleAttrList", spuSaleAttrList);
        model.addAttribute("valuesSkuJson", valuesSkuJson);

        return "item";
    }

}
