package com.unidata.uni.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** 全国硕士研究生招生考试国家线（按学科门类/专业学位类别） */
@Entity
@Table(name = "national_lines", indexes = {@Index(name = "idx_nl_year", columnList = "`year`")})
@Getter
@Setter
public class NationalLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "`year`", nullable = false)
    private Integer year;

    /** 学科门类/专业学位类别，如：哲学、金融、公共管理 */
    @Column(nullable = false, length = 120)
    private String discipline;

    /** 适用学科专业说明 */
    @Column(length = 200)
    private String subjects;

    /** A 类考生总分 */
    private Integer totalA;
    /** A 类单科（满分=100分） */
    private Integer oneA;
    /** A 类单科（满分>100分） */
    private Integer overA;

    /** B 类考生总分 */
    private Integer totalB;
    /** B 类单科（满分=100分） */
    private Integer oneB;
    /** B 类单科（满分>100分） */
    private Integer overB;

    @Column(length = 500)
    private String note;

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
