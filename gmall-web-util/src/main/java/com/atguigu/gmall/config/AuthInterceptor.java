package com.atguigu.gmall.config;

import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.util.HttpClientUtil;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.HttpClient;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

/**
 * @author zdy
 * @create 2019-08-27 20:41
 */
@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String token = request.getParameter("newToken");
        if(token!=null){
            CookieUtil.setCookie(request, response, "token", token, WebConst.COOKIE_MAXAGE, false);
        }

        if(token==null){
            token=CookieUtil.getCookieValue(request, "token", false);
        }

        if(token!=null){
            Map map = getUserMapByToken(token);
            String nickName = (String) map.get("nickName");
            request.setAttribute("nickName", nickName);

        }

        HandlerMethod handlerMethod=(HandlerMethod)handler;
        LoginRequie loginRequie = handlerMethod.getMethodAnnotation(LoginRequie.class);
        if(loginRequie!=null){
            String remoteAddr = request.getHeader("x-forwarded-for");
            String result = HttpClientUtil.doGet(WebConst.VERIFY_ADDRESS + "?token=" + token + "&currentIp=" + remoteAddr);

            if("success".equals(result)){
                Map map = getUserMapByToken(token);
                String userId =(String) map.get("userId");
                request.setAttribute("userId",userId);
                return true;

            }else {
                boolean b = loginRequie.autoRedirect();
                if(b){

                    String requestURL  = request.getRequestURL().toString();
                    String encodeRequestURL = URLEncoder.encode(requestURL, "UTF-8");
                    response.sendRedirect(WebConst.LOGIN_ADDRESS+"?originUrl="+encodeRequestURL);
                    return false;

                }
            }
        }



        return true;
    }

    private Map getUserMapByToken(String token) {

        String tokenUserInfo  = StringUtils.substringBetween(token, ".");

        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
        byte[] bytes = base64UrlCodec.decode(tokenUserInfo);

        String tokenUserInfoJSON=null;

        try {
            tokenUserInfoJSON = new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Map map = JSON.parseObject(tokenUserInfoJSON, Map.class);

        return map;
    }
}
