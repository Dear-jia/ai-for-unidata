package com.unidata.uni.service;

import com.unidata.uni.config.GlobalExceptionHandler.BizException;
import com.unidata.uni.entity.User;
import com.unidata.uni.repository.UserRepository;
import com.unidata.uni.security.CurrentUser;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User me() {
        return userRepository.findById(CurrentUser.id())
                .orElseThrow(() -> new BizException("用户不存在"));
    }

    @Transactional
    public User updateProfile(User patch) {
        User user = me();
        if (patch.getNickname() != null && !patch.getNickname().isBlank()) {
            user.setNickname(patch.getNickname());
        }
        if (patch.getEmail() != null) {
            user.setEmail(patch.getEmail());
        }
        if (patch.getPhone() != null) {
            user.setPhone(patch.getPhone());
        }
        return user;
    }

    @Transactional
    public void changePassword(String oldPassword, String newPassword) {
        User user = me();
        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BizException("原密码不正确");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
    }
}
