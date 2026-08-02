package com.unidata.uni.repository;

import com.unidata.uni.entity.Activity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ActivityRepository extends JpaRepository<Activity, Long> {

    Page<Activity> findByStatusOrderByCreatedAtDesc(Integer status, Pageable pageable);

    Page<Activity> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
