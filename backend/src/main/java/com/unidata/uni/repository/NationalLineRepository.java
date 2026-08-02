package com.unidata.uni.repository;

import com.unidata.uni.entity.NationalLine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NationalLineRepository extends JpaRepository<NationalLine, Long> {

    List<NationalLine> findByYearOrderByIdAsc(Integer year);

    List<NationalLine> findAllByOrderByYearDescIdAsc();

    @Query("select distinct n.year from NationalLine n order by n.year desc")
    List<Integer> findDistinctYears();
}
