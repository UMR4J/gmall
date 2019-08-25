package com.atguigu.gmall.list.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.SkuLsInfo;
import com.atguigu.gmall.bean.SkuLsParams;
import com.atguigu.gmall.bean.SkuLsResult;
import com.atguigu.gmall.service.ListService;
import io.searchbox.client.JestClient;
import io.searchbox.core.DocumentResult;
import io.searchbox.core.Index;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import io.searchbox.core.search.aggregation.MetricAggregation;
import io.searchbox.core.search.aggregation.TermsAggregation;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.TermsBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import javax.lang.model.element.VariableElement;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zdy
 * @create 2019-08-24 19:27
 */
@Service
public class ListServiceImpl implements ListService {

    @Autowired
    JestClient jestClient;

    public static final String ES_INDEX="gmall";

    public static final String ES_TYPE="SkuInfo";


    @Override
    public void saveSkuLsInfo(SkuLsInfo skuLsInfo) {

        Index index = new Index.Builder(skuLsInfo).index(ES_INDEX).type(ES_TYPE).id(skuLsInfo.getId()).build();

        try {
            DocumentResult documentResult = jestClient.execute(index);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }

    @Override
    public SkuLsResult search(SkuLsParams skuLsParams) {

        String query=makeQueryStringForSearch(skuLsParams);
        Search search = new Search.Builder(query).addIndex(ES_INDEX).addType(ES_TYPE).build();
        SearchResult searchResult=null;
        try {
            searchResult = jestClient.execute(search);
        } catch (IOException e) {
            e.printStackTrace();
        }

        SkuLsResult skuLsResult=makeResultForSearch(skuLsParams,searchResult);



        return skuLsResult;
    }

    private SkuLsResult makeResultForSearch(SkuLsParams skuLsParams, SearchResult searchResult) {

        SkuLsResult skuLsResult=new SkuLsResult();
        List<SkuLsInfo> skuLsInfoList=new ArrayList<>(skuLsParams.getPageSize());

        //setSkuLsInfoList
        List<SearchResult.Hit<SkuLsInfo, Void>> hits = searchResult.getHits(SkuLsInfo.class);
        for (SearchResult.Hit<SkuLsInfo, Void> hit : hits) {
            SkuLsInfo skuLsInfo = hit.source;
            if(hit.highlight!=null && hit.highlight.size()>0){
                List<String> list = hit.highlight.get("skuName");
                String skuNameHighLight = list.get(0);
                skuLsInfo.setSkuName(skuNameHighLight);
            }

            skuLsInfoList.add(skuLsInfo);
        }

        skuLsResult.setSkuLsInfoList(skuLsInfoList);
        //setTotal
        Long total = searchResult.getTotal();
        skuLsResult.setTotal(total);


        //计算总页数
        //long totalPages=(total+skuLsParams.getPageSize()-1)/skuLsParams.getPageSize();
        int pageSize = skuLsParams.getPageSize();
        long totalPages=total%pageSize==0?total/pageSize:(total/pageSize)+1;
        skuLsResult.setTotalPages(totalPages);


        //取出涉及的属性值id
        List<String> attrValueIdList=new ArrayList<>();

        MetricAggregation aggregations = searchResult.getAggregations();
        TermsAggregation groupbyAttr = aggregations.getTermsAggregation("groupby_attr");
        if(groupbyAttr!=null){
            List<TermsAggregation.Entry> buckets = groupbyAttr.getBuckets();
            for (TermsAggregation.Entry bucket : buckets) {
                String valueId = bucket.getKey();
                attrValueIdList.add(valueId);
            }
            skuLsResult.setAttrValueIdList(attrValueIdList);
        }

        return skuLsResult;
    }

    private String makeQueryStringForSearch(SkuLsParams skuLsParams) {

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        if(!StringUtils.isEmpty(skuLsParams.getKeyword())){

            MatchQueryBuilder skuNameMatchQueryBuilder = new MatchQueryBuilder("skuName", skuLsParams.getKeyword());
            boolQueryBuilder.must(skuNameMatchQueryBuilder);
            //高亮
            HighlightBuilder highlightBuilder = new HighlightBuilder();
            highlightBuilder.field("skuName");
            highlightBuilder.preTags("<span style='color:red'>");
            highlightBuilder.postTags("</span>");

            searchSourceBuilder.highlight(highlightBuilder);
        }

        //设置三级分类
        if(!StringUtils.isEmpty(skuLsParams.getCatalog3Id())){
            TermQueryBuilder termQueryBuilder = new TermQueryBuilder("catalog3Id", skuLsParams.getCatalog3Id());
            boolQueryBuilder.filter(termQueryBuilder);
        }

        //设置属性值
        if(!StringUtils.isEmpty(skuLsParams.getValueId())){

            for (String valueId : skuLsParams.getValueId()) {
                TermQueryBuilder termQueryBuilder = new TermQueryBuilder("skuAttrValueList.valueId", valueId);
                boolQueryBuilder.filter(termQueryBuilder);
            }

        }

        searchSourceBuilder.query(boolQueryBuilder);
        //设置分页
        int from = (skuLsParams.getPageNo()-1)*skuLsParams.getPageSize();
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(skuLsParams.getPageSize());

        //设置按热度排序
        searchSourceBuilder.sort("hotScore", SortOrder.DESC);

        //设置聚合
        TermsBuilder groupbyAttr = AggregationBuilders.terms("groupby_attr").field("skuAttrValueList.valueId");
        searchSourceBuilder.aggregation(groupbyAttr);

        String query = searchSourceBuilder.toString();
        System.out.println("query="+query);

        return query;
    }
}
