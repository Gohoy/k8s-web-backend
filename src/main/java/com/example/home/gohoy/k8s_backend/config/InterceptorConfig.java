package com.example.home.gohoy.k8s_backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
//        registry.addInterceptor(new AdminInterceptor())
//                .addPathPatterns("/admin/**");
//        registry.addInterceptor(new LoginInterceptor())
//                .addPathPatterns("/**")
//                .excludePathPatterns("/user/login", "/user/register", "/swagger-ui.html", "/swagger-ui/**",
//                        "/webjars/swagger-ui/**", "/v3/api-docs/**");
    }
}
