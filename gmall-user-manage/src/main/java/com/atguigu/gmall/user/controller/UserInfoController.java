package com.atguigu.gmall.user.controller;

import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @author zdy
 * @create 2019-08-14 19:39
 */
@RestController
public class UserInfoController {

    @Autowired
    private UserService userService;

    @GetMapping("findAll")
    public List<UserInfo> findAll() {
        return userService.findAll();
    }

    @GetMapping("getUserInfoByName")
    public UserInfo getUserInfoByName(String name) {
        return userService.getUserInfoByName(name);
    }

    @GetMapping("getUserInfoListByName")
    public List<UserInfo> getUserInfoListByName(UserInfo userInfo) {
        return userService.getUserInfoListByName(userInfo);
    }

    @GetMapping("getUserInfoListByNickName")
    public List<UserInfo> getUserInfoListByNickName(UserInfo userInfo){
        return userService.getUserInfoListByNickName(userInfo);
    }

    @RequestMapping("addUser")
    public void addUser(UserInfo userInfo) {
        userInfo.setEmail("1234@qq.com");
        userInfo.setName("1234");
        userInfo.setNickName("abcd");
        userService.addUser(userInfo);
        System.out.println("===="+userInfo.getId());
    }

    @RequestMapping("updUser")
    public void updUser(UserInfo userInfo) {
        userInfo.setId("5");
        userInfo.setEmail("12345678@qq.com");
        userInfo.setName("12345678");
        userInfo.setNickName("abcdfghj");
        userService.updUser(userInfo);

    }

    @RequestMapping("delUser")
    public void delUser(UserInfo userInfo) {
        userService.delUser(userInfo);
    }



}
