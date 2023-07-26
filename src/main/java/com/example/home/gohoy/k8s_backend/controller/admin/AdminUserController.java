package com.example.home.gohoy.k8s_backend.controller.admin;

import com.example.home.gohoy.k8s_backend.dto.UserDTO;
import com.example.home.gohoy.k8s_backend.service.UserService;
import com.example.home.gohoy.k8s_backend.utils.CommonResult;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@ApiResponse
@RequestMapping("/admin/")
public class AdminUserController {
    @Resource
    private UserService userService;

    @GetMapping("/getUserByName")
    @ApiResponse(description = "通过参数userName获取user数据")
    private CommonResult getUserByName(@RequestParam("userName") String userName){
        UserDTO user = userService.getUserByName(userName);
        if(user == null){
            return new CommonResult<UserDTO>().data(null).message("不存在用户 "+userName).code(404);
        }
        return new CommonResult<UserDTO>().data(user).message("获取用户 "+userName+" 成功").code(200);
    }

    @GetMapping("/getUsersByPage")
    @ApiResponse(description = "分页获取所有用户")
    private CommonResult getUsers(@RequestParam("pageNum") int pageNum,@RequestParam("pageSize")int pageSize){
        return new CommonResult<List<UserDTO>>().data(userService.getUsers(pageNum,pageSize)).code(200).message("第"+pageNum+"页的"+pageSize+"名用户获取成功");
    }
}
