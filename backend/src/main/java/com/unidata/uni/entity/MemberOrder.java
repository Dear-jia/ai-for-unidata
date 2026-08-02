package com.unidata.uni.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "member_orders")
@Getter
@Setter
public class MemberOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 40)
    private String orderNo;

    @Column(nullable = false)
    private Long userId;

    /** 月卡/季卡/年卡 */
    @Column(nullable = false, length = 20)
    private String plan;

    /** 会员时长(月) */
    @Column(nullable = false)
    private Integer months;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /** PENDING/PAID */
    @Column(nullable = false, length = 20)
    private String status = "PENDING";

    private LocalDateTime paidAt;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
