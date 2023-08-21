package com.example.home.gohoy.k8s_backend.controller.admin;

import com.example.home.gohoy.k8s_backend.dao.UserDao;
import com.example.home.gohoy.k8s_backend.dto.UserDTO;
import com.example.home.gohoy.k8s_backend.entities.User;
import com.example.home.gohoy.k8s_backend.service.user.UserService;
import com.example.home.gohoy.k8s_backend.utils.CommonResult;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin("*")
@ApiResponses
@RequestMapping("/admin/")
public class AdminUserController {
    @Resource
    private UserService userService;
    @Resource
    private UserDao userDao;
    @GetMapping("/getUserByName/{username}")
    @ApiResponse(description = "通过参数userName获取user数据")
    private CommonResult getUserByName(@PathVariable("username") String userName){
        UserDTO user = userService.getUserByName(userName);
        if(user == null){
            return new CommonResult<UserDTO>().data(null).message("不存在用户 "+userName).code(404);
        }
        return new CommonResult<UserDTO>().data(user).message("获取用户 "+userName+" 成功").code(200);
    }

    @GetMapping("/getUsersByPage/{pageNum}/{pageSize}")
    @ApiResponse(description = "分页获取所有用户")
    private CommonResult getUsers(@PathVariable("pageNum") int pageNum,@PathVariable("pageSize")int pageSize){
        return new CommonResult<List<UserDTO>>().data(userService.getUsers(pageNum,pageSize)).code(200).message("第"+pageNum+"页的"+pageSize+"名用户获取成功");
    }

    @PostMapping("/updateUser")
    @ApiResponse(description = "更新用户信息")
    public CommonResult updateUser(@RequestBody User user ){
        System.out.println(user);
        boolean update = userService.updateById(user);
if(update){
    return new CommonResult<>().message("更新用户信息成功").code(200);

}else {
    return new CommonResult<>().message("更新用户信息失败").code(500);

}
    }
    @PostMapping("deleteUser/{id}")
    @ApiResponse(description = "删除用户")
    public CommonResult deleteUser(@PathVariable("id") String id){
        int delete = userDao.deleteById(id);
        if(delete == 0){
            return new CommonResult<>().message("删除用户失败").code(500);
        }
        return new CommonResult<>().message("删除用户成功").code(200);
    }
}
