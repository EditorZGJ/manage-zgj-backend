package com.example.config;

import com.example.common.JwtUtil;
import com.example.common.Result;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 放行 OPTIONS 请求（CORS 预检）
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 从请求头取 token
        String token = request.getHeader("Authorization");
        if (token == null || !token.startsWith("Bearer ")) {
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write(new ObjectMapper().writeValueAsString(Result.error(401, "未登录")));
            return false;
        }

        try {
            token = token.substring(7); // 去掉 "Bearer "
            JwtUtil.parseToken(token);
            return true;
        } catch (Exception e) {
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().write(new ObjectMapper().writeValueAsString(Result.error(401, "token 无效或已过期")));
            return false;
        }
    }
}