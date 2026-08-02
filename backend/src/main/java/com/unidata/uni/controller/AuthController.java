package com.unidata.uni.controller;

import com.unidata.uni.dto.ApiResponse;
import com.unidata.uni.dto.AuthDtos.LoginRequest;
import com.unidata.uni.dto.AuthDtos.RegisterRequest;
import com.unidata.uni.entity.User;
import com.unidata.uni.security.CurrentUser;
import com.unidata.uni.service.AuthService;
import com.unidata.uni.service.UserService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    public AuthController(AuthService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/register")
    public ApiResponse<Map<String, Object>> register(@Valid @RequestBody RegisterRequest req) {
        return ApiResponse.ok(authService.register(req));
    }

    @PostMapping("/login")
    public ApiResponse<Map<String, Object>> login(@Valid @RequestBody LoginRequest req) {
        return ApiResponse.ok(authService.login(req));
    }

    @GetMapping("/me")
    public ApiResponse<User> me() {
        return ApiResponse.ok(userService.me());
    }

    @GetMapping("/vip-status")
    public ApiResponse<Map<String, Object>> vipStatus() {
        User user = userService.me();
        return ApiResponse.ok(Map.of(
                "vip", user.isVip(),
                "membershipType", user.getMembershipType(),
                "membershipExpireAt", user.getMembershipExpireAt(),
                "username", user.getUsername()));
    }
}
