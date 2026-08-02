package com.unidata.uni.repository;

import com.unidata.uni.entity.School;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SchoolRepository extends JpaRepository<School, Long> {

    Page<School> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<School> findByProvince(String province, Pageable pageable);

    Page<School> findByCategory(String category, Pageable pageable);

    Page<School> findByLevel(String level, Pageable pageable);

    Page<School> findByNameContainingIgnoreCaseAndProvince(String name, String province, Pageable pageable);

    Page<School> findByNameContainingIgnoreCaseAndCategory(String name, String category, Pageable pageable);

    Page<School> findByNameContainingIgnoreCaseAndLevel(String name, String level, Pageable pageable);

    Page<School> findByProvinceAndCategory(String province, String category, Pageable pageable);

    Page<School> findByProvinceAndLevel(String province, String level, Pageable pageable);

    Page<School> findByCategoryAndLevel(String category, String level, Pageable pageable);

    Page<School> findByNameContainingIgnoreCaseAndProvinceAndCategory(String name, String province, String category, Pageable pageable);

    Page<School> findByNameContainingIgnoreCaseAndProvinceAndLevel(String name, String province, String level, Pageable pageable);

    Page<School> findByNameContainingIgnoreCaseAndCategoryAndLevel(String name, String category, String level, Pageable pageable);

    Page<School> findByProvinceAndCategoryAndLevel(String province, String category, String level, Pageable pageable);

    Page<School> findByNameContainingIgnoreCaseAndProvinceAndCategoryAndLevel(String name, String province, String category, String level, Pageable pageable);

    List<School> findByStatusOrderByLevelDescIdAsc(Integer status, Pageable pageable);

    Optional<School> findFirstByName(String name);
}
