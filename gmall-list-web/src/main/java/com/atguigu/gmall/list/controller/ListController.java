package com.atguigu.gmall.list.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.BaseAttrInfo;
import com.atguigu.gmall.bean.BaseAttrValue;
import com.atguigu.gmall.bean.SkuLsParams;
import com.atguigu.gmall.bean.SkuLsResult;
import com.atguigu.gmall.service.ListService;
import com.atguigu.gmall.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author zdy
 * @create 2019-08-25 12:06
 */
@Controller
public class ListController {

    @Reference
    private ListService listService;

    @Reference
    private ManageService manageService;

    @RequestMapping("list.html")
    public String getList(SkuLsParams skuLsParams, Model model){

        // 设置每页显示的条数
        skuLsParams.setPageSize(2);


        SkuLsResult skuLsResult = listService.search(skuLsParams);

        model.addAttribute("skuLsInfoList", skuLsResult.getSkuLsInfoList());

        //根据结果中的属性id获取平台属性的列表
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        List<BaseAttrInfo> baseAttrInfoList=null;
        String catalog3Id = skuLsParams.getCatalog3Id();
        if(StringUtils.isEmpty(catalog3Id)){
            baseAttrInfoList=manageService.getAttrList(attrValueIdList);
        }else {
            baseAttrInfoList=manageService.getAttrList(catalog3Id);
        }
        model.addAttribute("baseAttrInfoList", baseAttrInfoList);

        //根据传入的参数制作url?后的参数
        String urlParam = makeUrlParam(skuLsParams);
        model.addAttribute("urlParam",urlParam);

        // 面包屑列表
        List<BaseAttrValue> baseAttrValueSelectdList = new ArrayList<>();


        for (Iterator<BaseAttrInfo> iterator = baseAttrInfoList.iterator(); iterator.hasNext(); ) {
            BaseAttrInfo baseAttrInfo = iterator.next();
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            for (BaseAttrValue baseAttrValue : attrValueList) {
                baseAttrValue.setUrlParam(urlParam);
                if(skuLsParams.getValueId()!=null && skuLsParams.getValueId().length>0){

                    for (String valueId : skuLsParams.getValueId()) {

                        if(valueId.equals(baseAttrValue.getId())){
                            iterator.remove();
                            //构造面包屑
                            BaseAttrValue baseAttrValueSelectd = new BaseAttrValue();
                            baseAttrValueSelectd.setValueName(baseAttrInfo.getAttrName()+":"+baseAttrValue.getValueName());

                            //制作回退的urlParams
                            String backUrlParam = makeUrlParam(skuLsParams, valueId);
                            baseAttrValueSelectd.setUrlParam(backUrlParam);
                            //添加到面包屑列表
                            baseAttrValueSelectdList.add(baseAttrValueSelectd);
                        }

                    }

                }
            }

        }
        model.addAttribute("keyword",   skuLsParams.getKeyword());
        model.addAttribute("baseAttrValueSelectdList", baseAttrValueSelectdList);

        //分页
        model.addAttribute("pageNo", skuLsParams.getPageNo());
        model.addAttribute("totalPages", skuLsResult.getTotalPages());


        return "list";

    }

    private String makeUrlParam(SkuLsParams skuLsParams,String... excludeValueIds) {
        String urlParam="";

        String catalog3Id = skuLsParams.getCatalog3Id();
        if(!StringUtils.isEmpty(catalog3Id)){
            if (urlParam.length()>0){
                urlParam+="&";
            }

            urlParam+="catalog3Id="+catalog3Id;
        }
        String keyword = skuLsParams.getKeyword();
        if(!StringUtils.isEmpty(keyword)){
            if (urlParam.length()>0){
                urlParam+="&";
            }

            urlParam+="keyword="+keyword;
        }

        String[] valueIds = skuLsParams.getValueId();
        if(valueIds!=null && valueIds.length>0){

            for (String valueId : valueIds) {

                if(excludeValueIds!=null && excludeValueIds.length>0){
                    String excludeValueId = excludeValueIds[0];
                    if(valueId.equals(excludeValueId)){
                        continue;
                    }
                }

                if (urlParam.length()>0){
                    urlParam+="&";
                }

                urlParam+="valueId="+valueId;
            }

        }


        return urlParam;
    }
}
