package com.example.home.gohoy.k8s_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.home.gohoy.k8s_backend.dto.UserDTO;
import com.example.home.gohoy.k8s_backend.entities.User;

public interface UserService extends IService<User> {
    public UserDTO getUserByName(String userName);
}
