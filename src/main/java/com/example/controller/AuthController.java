package com.example.controller;

import com.example.common.JwtUtil;
import com.example.common.Result;
import com.example.entity.AuthUser;
import com.example.mapper.AuthUserMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthController {

    private final AuthUserMapper authUserMapper;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthUserMapper authUserMapper, PasswordEncoder passwordEncoder) {
        this.authUserMapper = authUserMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/auth/register")
    public Result<String> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        // 检查用户名是否已存在
        AuthUser existing = authUserMapper.selectOne(
            new LambdaQueryWrapper<AuthUser>().eq(AuthUser::getUsername, username)
        );
        if (existing != null) {
            return Result.error(400, "用户名已存在");
        }

        // 密码加密后存入数据库
        AuthUser user = new AuthUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        authUserMapper.insert(user);
        return Result.success("注册成功");
    }

    @PostMapping("/auth/login")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        // 查用户
        AuthUser user = authUserMapper.selectOne(
            new LambdaQueryWrapper<AuthUser>().eq(AuthUser::getUsername, username)
        );
        if (user == null) {
            return Result.error(400, "用户不存在");
        }

        // 校验密码（BCrypt 加密比对）
        if (!passwordEncoder.matches(password, user.getPassword())) {
            return Result.error(400, "密码错误");
        }

        // 生成 token
        String token = JwtUtil.generate(user.getId(), user.getUsername());
        return Result.success(Map.of(
            "token", token,
            "username", user.getUsername()
        ));
    }
}