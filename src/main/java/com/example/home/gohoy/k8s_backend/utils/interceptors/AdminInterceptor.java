package com.example.home.gohoy.k8s_backend.utils.interceptors;

import com.example.home.gohoy.k8s_backend.utils.JWTUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletResponse;

@Component
public class AdminInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(jakarta.servlet.http.HttpServletRequest request, jakarta.servlet.http.HttpServletResponse response, Object handler) throws Exception {
        if(request.getMethod().equals("OPTIONS")){
            return true;
        }
        System.out.println("AdminInterceptor");
        // 从请求头部获取 Authorization Cookie
        String token = request.getHeader("Authorization");

        if  (JWTUtils.verifyToken(token).get("isAdmin").equals(1)){
            return true;
        }else {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return false;
        }
    }



}
