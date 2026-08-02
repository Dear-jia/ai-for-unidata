package com.unidata.uni.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "score_lines",
        indexes = {@Index(name = "idx_school_year", columnList = "schoolId,`year`")})
@Getter
@Setter
public class ScoreLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long schoolId;

    @Column(length = 100)
    private String schoolName;

    @Column(name = "`year`", nullable = false)
    private Integer year;

    @Column(nullable = false, length = 100)
    private String major;

    /** 复试线/国家线/校线/院线 */
    @Column(length = 20)
    private String lineType = "复试线";

    /** 总分 */
    private Integer minScore;

    /** 政治 */
    private Integer politicalScore;

    /** 外语 */
    private Integer foreignScore;

    /** 业务课一 */
    private Integer majorScore1;

    /** 业务课二 */
    private Integer majorScore2;

    @Column(length = 500)
    private String remark;

    /** true=仅会员可见 */
    @Column(nullable = false)
    private Boolean premium = Boolean.TRUE;

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
