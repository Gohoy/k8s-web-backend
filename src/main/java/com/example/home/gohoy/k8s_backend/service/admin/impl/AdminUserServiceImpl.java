package com.example.home.gohoy.k8s_backend.service.admin.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.home.gohoy.k8s_backend.dao.UserDao;
import com.example.home.gohoy.k8s_backend.entities.User;
import com.example.home.gohoy.k8s_backend.service.admin.AdminUserService;

public class AdminUserServiceImpl extends ServiceImpl<UserDao,User> implements AdminUserService {

}
