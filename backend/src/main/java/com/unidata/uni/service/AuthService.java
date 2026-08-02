package com.unidata.uni.service;

import com.unidata.uni.config.GlobalExceptionHandler.BizException;
import com.unidata.uni.dto.AuthDtos.LoginRequest;
import com.unidata.uni.dto.AuthDtos.RegisterRequest;
import com.unidata.uni.entity.User;
import com.unidata.uni.repository.UserRepository;
import com.unidata.uni.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public Map<String, Object> register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new BizException("用户名已被注册");
        }
        User user = new User();
        user.setUsername(req.username());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setNickname(req.nickname() == null || req.nickname().isBlank() ? req.username() : req.nickname());
        user.setEmail(req.email());
        user.setPhone(req.phone());
        userRepository.save(user);
        return buildTokenMap(user);
    }

    public Map<String, Object> login(LoginRequest req) {
        User user = userRepository.findByUsername(req.username())
                .orElseThrow(() -> new BizException("用户名或密码错误"));
        if (!passwordEncoder.matches(req.password(), user.getPassword())) {
            throw new BizException("用户名或密码错误");
        }
        if (user.getStatus() == null || user.getStatus() != 1) {
            throw new BizException("账号已被禁用，请联系管理员");
        }
        return buildTokenMap(user);
    }

    private Map<String, Object> buildTokenMap(User user) {
        String token = jwtService.generateToken(user.getId(), user.getUsername(), user.getRole());
        return Map.of(
                "token", token,
                "user", user);
    }
}
