package com.unidata.uni.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** 院校官方复试线原始来源（研招网发布的官方图表/原文） */
@Entity
@Table(name = "score_sources", indexes = {@Index(name = "idx_ss_school_year", columnList = "schoolId,`year`")})
@Getter
@Setter
public class ScoreSource {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long schoolId;

    @Column(length = 100)
    private String schoolName;

    @Column(name = "`year`", nullable = false)
    private Integer year;

    @Column(length = 300)
    private String title;

    /** 官方分数线图片地址 */
    @Column(length = 500)
    private String imageUrl;

    /** 官方原文页面地址 */
    @Column(length = 500)
    private String sourceUrl;

    private Integer sort;

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
