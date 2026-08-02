package com.unidata.uni.repository;

import com.unidata.uni.entity.ScoreLine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ScoreLineRepository extends JpaRepository<ScoreLine, Long> {

    List<ScoreLine> findBySchoolIdOrderByYearDesc(Long schoolId);

    Page<ScoreLine> findBySchoolIdAndYear(Long schoolId, Integer year, Pageable pageable);

    Page<ScoreLine> findBySchoolId(Long schoolId, Pageable pageable);

    Page<ScoreLine> findByMajorContainingIgnoreCase(String major, Pageable pageable);

    Page<ScoreLine> findByMajorContainingIgnoreCaseOrRemarkContainingIgnoreCase(String major, String remark, Pageable pageable);

    boolean existsBySchoolIdAndYearAndLineType(Long schoolId, Integer year, String lineType);

    void deleteBySchoolIdAndYearAndLineType(Long schoolId, Integer year, String lineType);

    long deleteByYearLessThan(Integer year);

    void deleteBySchoolId(Long schoolId);

    long deleteByLineTypeAndMajor(String lineType, String major);

    long deleteByLineTypeAndRemarkContaining(String lineType, String remark);

    List<ScoreLine> findTop12ByLineTypeNotOrderByCreatedAtDesc(String lineType);

    Page<ScoreLine> findByLineTypeNotOrderByCreatedAtDesc(String lineType, Pageable pageable);

    @Query("select distinct s.year from ScoreLine s order by s.year desc")
    List<Integer> findDistinctYears();

    List<ScoreLine> findTop12ByOrderByCreatedAtDesc();

    boolean existsBySchoolIdAndYearAndMajor(Long schoolId, Integer year, String major);
}
