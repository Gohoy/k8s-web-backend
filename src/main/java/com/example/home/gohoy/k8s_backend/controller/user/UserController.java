package com.example.home.gohoy.k8s_backend.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.home.gohoy.k8s_backend.dao.UserDao;
import com.example.home.gohoy.k8s_backend.entities.User;
import com.example.home.gohoy.k8s_backend.service.UserService;
import com.example.home.gohoy.k8s_backend.utils.CommonResult;
import com.example.home.gohoy.k8s_backend.utils.JWTUtils;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.sql.Timestamp;

@ApiResponses
@RestController("com.example.home.gohoy.k8s_backend")
@RequestMapping("/user/")
public class UserController {
    @Resource
    private UserDao userDao;
    @Resource
    private UserService userService;

    @PostMapping("/register")
    @ApiResponse(description = "用户注册")
    private CommonResult register(@RequestBody User user){
        String token = JWTUtils.generateToken(user.getUsername(), 604800000);//token 一周过期
        user.setToken(token);
        user.setLastLogin(new Timestamp(System.currentTimeMillis()));
        int insert = userDao.insert(user);
        if (insert == 1) {
            return new CommonResult<User>().data(user).message("注册成功").code(200);
        }
        return new CommonResult<>().message("注册失败，用户名已存在").code(500);
    }
    @PostMapping("/login")
    @ApiResponse(description = "用户登录")
    private CommonResult login(@RequestBody User user){
        if(userService.getUserByName(user.getUsername()).getPassword().equals( user.getPassword()) && JWTUtils.verifyToken(user.getToken()).get("username").equals(user.getUsername())){
            String token = JWTUtils.generateToken(user.getUsername(), 604800000);//token 一周过期
            user.setLastLogin(new Timestamp(System.currentTimeMillis()));
            user.setToken(token);
            userDao.update(user,new QueryWrapper<User>().eq("username",user.getUsername()));
            return new CommonResult<String>().data(token).message("登录成功").code(200);
        }
        return  new CommonResult<>().message("用户名或密码错误，请重试").code(400);
    }


}
