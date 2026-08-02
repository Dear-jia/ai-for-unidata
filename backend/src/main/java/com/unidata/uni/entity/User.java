package com.unidata.uni.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @JsonIgnore
    @Column(nullable = false, length = 100)
    private String password;

    @Column(length = 50)
    private String nickname;

    @Column(length = 100)
    private String email;

    @Column(length = 20)
    private String phone;

    /** USER / ADMIN */
    @Column(nullable = false, length = 20)
    private String role = "USER";

    /** 1=正常 0=禁用 */
    @Column(nullable = false)
    private Integer status = 1;

    /** FREE / VIP */
    @Column(nullable = false, length = 20)
    private String membershipType = "FREE";

    private LocalDateTime membershipExpireAt;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = createdAt;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public boolean isVip() {
        return "VIP".equals(membershipType)
                && membershipExpireAt != null
                && membershipExpireAt.isAfter(LocalDateTime.now());
    }
}
