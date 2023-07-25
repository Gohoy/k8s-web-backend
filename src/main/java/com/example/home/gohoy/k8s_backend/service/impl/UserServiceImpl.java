package com.example.home.gohoy.k8s_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.home.gohoy.k8s_backend.dao.UserDao;
import com.example.home.gohoy.k8s_backend.dto.UserDTO;
import com.example.home.gohoy.k8s_backend.entities.User;
import com.example.home.gohoy.k8s_backend.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl extends ServiceImpl<UserDao , User> implements UserService {

    private UserDao userDao;
    @Override
    public UserDTO getUserByName(String userName) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",userName);
        return userDao.selectOne(queryWrapper);
    }
}
