package com.example.home.gohoy.k8s_backend.utils.interceptors;

import com.example.home.gohoy.k8s_backend.service.UserService;
import jakarta.servlet.http.Cookie;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletResponse;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;
    @Override
    public boolean preHandle(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, Object handler) throws Exception {

        System.out.println("HandlerInterceptor");
        // 从请求头部获取 Authorization Cookie
        Cookie[] cookies = request.getCookies();
        // 从请求头部获取 X-Username Cookie
        String username = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("X-Username")) {
                    username = cookie.getValue();
                    break;
                }
            }
        }

        if(userService != null &&  userService.getUserByName(username).getIsAdmin() == '1'){
            return true;
        }else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
    }



}
