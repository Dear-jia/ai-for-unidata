package com.unidata.uni.repository;

import com.unidata.uni.entity.MemberOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MemberOrderRepository extends JpaRepository<MemberOrder, Long> {

    List<MemberOrder> findByUserIdOrderByCreatedAtDesc(Long userId);

    Optional<MemberOrder> findByOrderNo(String orderNo);

    List<MemberOrder> findTop5ByOrderByCreatedAtDesc();
}
