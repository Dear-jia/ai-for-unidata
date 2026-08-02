package com.unidata.uni.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record LoginRequest(
            @NotBlank(message = "用户名不能为空") String username,
            @NotBlank(message = "密码不能为空") String password) {
    }

    public record RegisterRequest(
            @NotBlank(message = "用户名不能为空")
            @Pattern(regexp = "^[a-zA-Z0-9_]{3,20}$", message = "用户名需为3-20位字母、数字或下划线")
            String username,
            @NotBlank(message = "密码不能为空")
            @Size(min = 6, max = 32, message = "密码长度需在6-32位之间")
            String password,
            @Size(max = 50, message = "昵称最长50字符") String nickname,
            @Email(message = "邮箱格式不正确") String email,
            @Pattern(regexp = "^$|^1[3-9]\\d{9}$", message = "手机号格式不正确") String phone) {
    }

    public record MemberOrderRequest(
            @NotBlank(message = "请选择会员套餐")
            @Pattern(regexp = "MONTH|QUARTER|YEAR", message = "套餐参数不正确")
            String plan) {
    }
}
