package com.example.home.gohoy.k8s_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.home.gohoy.k8s_backend.dao.UserDao;
import com.example.home.gohoy.k8s_backend.dto.UserDTO;
import com.example.home.gohoy.k8s_backend.service.UserService;
import com.example.home.gohoy.k8s_backend.utils.CommonResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Api("userAPI")
@RestController
@RequestMapping("/user/")
public class UserController {
    @Autowired
    private UserDao userDao;
    @Autowired
    private UserService userService;

    @GetMapping("/getUserByName/")
    @ApiOperation("通过参数userName获取user数据")
    private CommonResult getUserByName(@RequestParam("userName") String userName){
        return new CommonResult().data(userService.getUserByName(userName)).message("获取用户 "+userName+" 成功").code(200);
    }

    @GetMapping("getUsersByPage")
    @ApiOperation("分页获取所有用户")
    private CommonResult getUsers(){

    }

}
