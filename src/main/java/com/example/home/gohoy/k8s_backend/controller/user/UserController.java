package com.example.home.gohoy.k8s_backend.controller.user;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.home.gohoy.k8s_backend.POJO.PodInfo;
import com.example.home.gohoy.k8s_backend.dao.UserDao;
import com.example.home.gohoy.k8s_backend.dto.UserDTO;
import com.example.home.gohoy.k8s_backend.entities.User;
import com.example.home.gohoy.k8s_backend.service.UserService;
import com.example.home.gohoy.k8s_backend.utils.CommonResult;
import com.example.home.gohoy.k8s_backend.utils.JWTUtils;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.List;

@CrossOrigin("*")
@ApiResponses
@RestController("com.example.home.gohoy.k8s_backend")
@RequestMapping("/user/")
public class UserController {
    @Resource
    private UserDao userDao;
    @Resource
    private UserService userService;
    @Resource
    private PodController podController;

    @PostMapping("/register")
    @ApiResponse(description = "用户注册")
    private CommonResult register(@RequestBody User user) {
        String token = JWTUtils.generateToken(user.getUsername(), (byte) '0', 604800000);//token 一周过期
        user.setToken(token);
        user.setLastLogin(new Timestamp(System.currentTimeMillis()));

        try {
            int insert = userDao.insert(user);
            if (insert == 1) {
                return new CommonResult<User>().data(user).message("注册成功").code(200);
            } else {
                return new CommonResult<>().message("注册失败，未知错误").code(500);
            }
        } catch (DuplicateKeyException e) {
            // 用户名重复时，捕获异常，并返回相应的错误信息
            return new CommonResult<>().message("注册失败，用户名已存在").code(500);
        }
    }

    @PostMapping("/login")
    @ApiResponse(description = "用户登录")
    private CommonResult login(@RequestBody User user){
        User user1 = userService.getUserByName(user.getUsername());
        if(user1 == null){
            return  new CommonResult<>().message("用户名或密码错误，请重试").code(500);
        }
        if(user1.getPassword().equals(user.getPassword() )){
            String token = JWTUtils.generateToken(user.getUsername(), user1.getIsAdmin(), 604800000);//token 一周过期
            user1.setLastLogin(new Timestamp(System.currentTimeMillis()));
            user1.setToken(token);
            List<PodInfo> pods = podController.getPodsByUserName(user.getUsername());
            int ctrCount = 0;
            int vmCount = 0;
            for (PodInfo pod : pods) {
                if(pod.getName().startsWith(user.getUsername()+"-vm-")){
                    vmCount ++;
                }else if(pod.getName().startsWith(user.getUsername() + "-ctr-")){
                    ctrCount++;
                }
            }
            user1.setCtrOccupied(ctrCount);
            user1.setVmOccupied(vmCount);
            userDao.update(user1,new QueryWrapper<User>().eq("username",user.getUsername()));
            return new CommonResult<String>().data(token).message("登录成功").code(200);
        }
        return  new CommonResult<>().message("用户名或密码错误，请重试").code(500);
    }

    @GetMapping("/getUserDTO/{username}")
    @ApiResponse(description = "获取用户可用的container数量和虚拟机数量")
    private CommonResult getUser(@PathVariable("username") String username){
        UserDTO user = userService.getUserByName(username);
        return new CommonResult<UserDTO>().data(user).message("获取用户数据成功").code(200);
    }

    @GetMapping("/index")
    public ResponseEntity<String> getIndex() throws IOException {
        String markdownContent = readMarkdownFile("src/main/java/com/example/home/gohoy/k8s_backend/assets/index.md");
        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_PLAIN)
                .body(markdownContent);
    }

    private String readMarkdownFile(String filePath) throws IOException {
        // 读取Markdown文件的内容
        byte[] contentBytes = Files.readAllBytes(Paths.get(filePath));
        return new String(contentBytes, StandardCharsets.UTF_8);
    }

}
