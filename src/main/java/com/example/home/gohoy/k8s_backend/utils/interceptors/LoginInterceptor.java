package com.example.home.gohoy.k8s_backend.utils.interceptors;

import com.example.home.gohoy.k8s_backend.utils.JWTUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        // 在这里进行鉴权操作，判断是否具有访问权限
        System.out.println("loginInterceptor");
        // 从请求头部获取 Authorization Cookie
        String token = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("Authorization")) {
                    token = cookie.getValue();
                    break;
                }
            }
        }

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

        // 根据需求处理 token 和 username
        if (token != null && username != null && JWTUtils.verifyToken(token).get("username").equals(username)) {
            // 鉴权逻辑，验证 token 和 username 的有效性
            // 如果鉴权成功，可以返回 true，允许请求继续执行
            return true;

        } else {
            // 如果鉴权失败，可以返回 false，阻止请求继续执行
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
    }


}
