package com.example.controller;

import com.example.common.JwtUtil;
import com.example.common.Result;
import com.example.entity.AuthUser;
import com.example.mapper.AuthUserMapper;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthController {

    private final AuthUserMapper authUserMapper;

    public AuthController(AuthUserMapper authUserMapper) {
        this.authUserMapper = authUserMapper;
    }

    @PostMapping("/auth/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        // 查用户
        AuthUser user = authUserMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<AuthUser>()
                .eq(AuthUser::getUsername, username)
        );
        if (user == null) {
            return Result.error(400, "用户不存在");
        }
        if (!user.getPassword().equals(password)) {
            return Result.error(400, "密码错误");
        }

        // 生成 token
        String token = JwtUtil.generateToken(user.getId(), user.getUsername());
        return Result.success(Map.of(
            "token", token,
            "username", user.getUsername()
        ));
    }
}