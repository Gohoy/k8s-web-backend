package com.example.home.gohoy.k8s_backend.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.home.gohoy.k8s_backend.dao.UserDao;
import com.example.home.gohoy.k8s_backend.dto.UserDTO;
import com.example.home.gohoy.k8s_backend.entities.User;
import com.example.home.gohoy.k8s_backend.service.user.UserService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl extends ServiceImpl<UserDao , User> implements UserService {

    @Resource
    private UserDao userDao;
    @Override
    public User getUserByName(String userName) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",userName);
        return userDao.selectOne(queryWrapper);
    }

    @Override
    public List<UserDTO> getUsers(int pageNum, int size) {
        Page<User> page = new Page<User>(pageNum , size);
        List<User> userPage = userDao.selectPage(page, null).getRecords();
        List<UserDTO> returnList = new ArrayList<>();
        for(User user : userPage){
            returnList.add((UserDTO) user);
        }
        return returnList;
    }

}
