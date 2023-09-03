package com.example.home.gohoy.k8s_backend.test;

import com.example.home.gohoy.k8s_backend.entities.User;
import com.example.home.gohoy.k8s_backend.service.kubevirt.KubevirtApi;
import com.example.home.gohoy.k8s_backend.service.user.UserService;
import com.example.home.gohoy.k8s_backend.utils.CommonResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@ApiResponses
@RequestMapping("vm")
public class KubevirtTest {

    @Resource
    private KubevirtApi kubevirtApi;

    @Resource
    private UserService userService;
    @ApiResponse(description = "测试api，弃用")
    @PostMapping("/creatVM/{username}")
    private CommonResult createVM(@PathVariable("username") String username) throws JsonProcessingException {
        User user = userService.getUserByName(username);
        if (user.getVmMax() - user.getVmOccupied() <= 0) {
            return new CommonResult<>().message("当前用户可用虚拟机已达上限").code(500);
        }
        String vm = kubevirtApi.getVM(username, user.getVmOccupied().toString());
        return new CommonResult<>().data(vm).code(200).message("获取vm成功");
    }


}
