package com.atguigu.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.passport.config.JwtUtil;
import com.atguigu.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zdy
 * @create 2019-08-27 16:32
 */
@Controller
public class PassportController {

    @Value("${token.key}")
    private String signKey;

    @Reference
    private UserService userService;



    @RequestMapping("index")
    public String index(HttpServletRequest request){

        String originUrl = request.getParameter("originUrl");

        request.setAttribute("originUrl", originUrl);


        return "index";
    }

    @ResponseBody
    @RequestMapping("login")
    public String login(HttpServletRequest request,UserInfo userInfo){

        String remoteAddr  = request.getHeader("X-forwarded-for");
        System.out.println("remoteAddr:"+remoteAddr);
        if(userInfo!=null){
            UserInfo loginUser=userService.login(userInfo);
            if(loginUser!=null){

                Map<String,Object> map=new HashMap<>();
                map.put("userId", loginUser.getId());
                map.put("nickName", loginUser.getNickName());

                String token = JwtUtil.encode(signKey, map, remoteAddr);

                System.out.println("登录成功返回的token："+token);

                return token;

            }
        }

        return "fail";
    }

    @RequestMapping("verify")
    @ResponseBody
    public String verify(HttpServletRequest request){

        String token = request.getParameter("token");
        String currentIp = request.getParameter("currentIp");
        if(token!=null){
            Map<String, Object> map = JwtUtil.decode(token, signKey, currentIp);
            if(map!=null){
                String userId =(String) map.get("userId");
                UserInfo userInfo = userService.verify(userId);
                if(userInfo!=null){
                    return "success";
                }
            }

        }


        return "fail";
    }

}
