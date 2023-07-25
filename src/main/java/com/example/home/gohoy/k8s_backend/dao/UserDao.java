package com.example.home.gohoy.k8s_backend.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.home.gohoy.k8s_backend.entities.User;
import jakarta.persistence.Table;

@Table(name = "users")
public interface UserDao extends BaseMapper<User> {
}
