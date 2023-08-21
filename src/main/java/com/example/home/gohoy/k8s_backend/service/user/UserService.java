package com.example.home.gohoy.k8s_backend.service.user;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.home.gohoy.k8s_backend.dto.UserDTO;
import com.example.home.gohoy.k8s_backend.entities.User;

import java.util.List;

public interface UserService extends IService<User> {
//    通过用户名查询user
     User getUserByName(String userName);
//  分页查询所有user
     List<UserDTO> getUsers (int pageNum, int size);
}
