package com.unidata.uni.repository;

import com.unidata.uni.entity.ScoreSource;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ScoreSourceRepository extends JpaRepository<ScoreSource, Long> {

    List<ScoreSource> findBySchoolIdOrderByYearDescSortAsc(Long schoolId);
}
