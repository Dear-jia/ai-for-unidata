package com.unidata.uni.repository;

import com.unidata.uni.entity.ScoreSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ScoreSourceRepository extends JpaRepository<ScoreSource, Long> {

    List<ScoreSource> findBySchoolIdOrderByYearDescSortAsc(Long schoolId);

    @Query("select distinct s.schoolId from ScoreSource s")
    List<Long> findDistinctSchoolIds();
}
