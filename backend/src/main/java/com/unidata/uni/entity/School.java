package com.unidata.uni.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "schools")
@Getter
@Setter
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 50)
    private String province;

    @Column(length = 50)
    private String city;

    /** 主管部门（如：教育部 / 北京市） */
    @Column(length = 100)
    private String dept;

    /** 综合/理工/师范/医药/财经/农林/政法/艺术/语言/民族 */
    @Column(length = 20)
    private String category;

    /** 985/211/双一流/普通本科 */
    @Column(length = 50)
    private String level;

    @Column(length = 200)
    private String logo;

    /** 招生官网/研招网招生细则页面地址 */
    @Column(length = 500)
    private String admissionUrl;

    @Column(length = 2000)
    private String intro;

    @Column(nullable = false)
    private Integer status = 1;

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
}
