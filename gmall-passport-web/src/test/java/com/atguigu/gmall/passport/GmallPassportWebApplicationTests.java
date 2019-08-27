package com.atguigu.gmall.passport;

import com.atguigu.gmall.passport.config.JwtUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallPassportWebApplicationTests {

    @Test
    public void testJWT() {

        String key = "atguigu";
        String ip="192.168.232.136";

        Map map = new HashMap();
        map.put("userId","1001");
        map.put("nickName","marry");

        String token = JwtUtil.encode(key, map, ip);
        System.out.println(token);

        Map<String, Object> decode = JwtUtil.decode(token, key, "192.168.232.136");

        System.out.println(decode);

    }

}
