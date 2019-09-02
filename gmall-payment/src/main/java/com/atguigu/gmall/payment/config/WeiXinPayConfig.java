package com.atguigu.gmall.payment.config;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

/**
 * @author zdy
 * @create 2019-09-01 18:44
 */
@Configuration
@PropertySource("classpath:weixin.properties")
public class WeiXinPayConfig implements InitializingBean {

    // 服务号Id
    @Value("${appid}")
    private String appid;
    // 商户号Id
    @Value("${partner}")
    private String partner;
    // 密钥
    @Value("${partnerkey}")
    private String partnerkey;



    public static String APPID;

    public static String PARTNER;

    public static String PARTNERKEY;



    @Override
    public void afterPropertiesSet() throws Exception {
        APPID=appid;
        PARTNER=partner;
        PARTNERKEY=partnerkey;
    }
}
