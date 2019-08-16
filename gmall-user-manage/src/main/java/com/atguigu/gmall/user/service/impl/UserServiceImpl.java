package com.atguigu.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @author zdy
 * @create 2019-08-14 19:37
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Override
    public List<UserInfo> findAll() {
        return userInfoMapper.selectAll();
    }

    @Override
    public UserInfo getUserInfoByName(String name) {

        Example example=new Example(UserInfo.class);
        example.createCriteria().andEqualTo("name", name);
        return userInfoMapper.selectByExample(example).get(0);
    }

    @Override
    public List<UserInfo> getUserInfoListByName(UserInfo userInfo) {
        return userInfoMapper.select(userInfo);
    }

    @Override
    public List<UserInfo> getUserInfoListByNickName(UserInfo userInfo) {
        Example example=new Example(UserInfo.class);
        example.createCriteria().andLike("nickName", "%"+userInfo.getNickName()+"%");
        return userInfoMapper.selectByExample(example);
    }

    @Override
    public void addUser(UserInfo userInfo) {

        //userInfoMapper.insertSelective(userInfo);
        userInfoMapper.insert(userInfo);

    }

    @Override
    public void updUser(UserInfo userInfo) {
        userInfoMapper.updateByPrimaryKeySelective(userInfo);
    }

    @Override
    public void delUser(UserInfo userInfo) {
        userInfoMapper.delete(userInfo);
    }

    @Override
    public List<UserAddress> getUserAddressListByUserId(UserAddress userAddress) {
        return userAddressMapper.select(userAddress);
    }
}
