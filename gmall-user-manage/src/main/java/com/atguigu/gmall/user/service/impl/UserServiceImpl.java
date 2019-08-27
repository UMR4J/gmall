package com.atguigu.gmall.user.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.UserAddress;
import com.atguigu.gmall.bean.UserInfo;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.service.UserService;
import com.atguigu.gmall.user.mapper.UserAddressMapper;
import com.atguigu.gmall.user.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @author zdy
 * @create 2019-08-14 19:37
 */
@Service
public class UserServiceImpl implements UserService {

    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int userKey_timeOut=60*60*24;


    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private UserAddressMapper userAddressMapper;

    @Autowired
    private RedisUtil redisUtil;

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

    @Override
    public UserInfo login(UserInfo userInfo) {

        String password = DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes());
        userInfo.setPasswd(password);

        Jedis jedis=null;

        try {
            UserInfo loginUser = userInfoMapper.selectOne(userInfo);
            if(loginUser!=null){

                String userJsonString = JSON.toJSONString(loginUser);
                jedis = redisUtil.getJedis();
                String userKey=userKey_prefix+loginUser.getId()+userinfoKey_suffix;
                jedis.setex(userKey, userKey_timeOut, userJsonString);

                return loginUser;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(jedis!=null){
                jedis.close();
            }
        }

        return null;
    }

    @Override
    public UserInfo verify(String userId) {

        Jedis jedis=null;

        try {
            jedis = redisUtil.getJedis();
            String key = userKey_prefix+userId+userinfoKey_suffix;
            String userInfoStr = jedis.get(key);
            if(userInfoStr!=null){
                jedis.expire(key, userKey_timeOut);
                UserInfo userInfo = JSON.parseObject(userInfoStr, UserInfo.class);
                return userInfo;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis!=null){
                jedis.close();
            }
        }


        return null;
    }
}
