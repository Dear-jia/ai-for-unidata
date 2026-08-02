package com.unidata.uni.controller;

import com.unidata.uni.dto.ApiResponse;
import com.unidata.uni.entity.User;
import com.unidata.uni.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ApiResponse<User> me() {
        return ApiResponse.ok(userService.me());
    }

    @PutMapping("/profile")
    public ApiResponse<User> updateProfile(@RequestBody User patch) {
        return ApiResponse.ok(userService.updateProfile(patch));
    }

    @PutMapping("/password")
    public ApiResponse<Void> changePassword(@RequestBody Map<String, String> body) {
        userService.changePassword(body.get("oldPassword"), body.get("newPassword"));
        return ApiResponse.ok();
    }
}
