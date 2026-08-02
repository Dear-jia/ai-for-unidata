package com.unidata.uni.repository;

import com.unidata.uni.entity.Article;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ArticleRepository extends JpaRepository<Article, Long> {

    Page<Article> findByStatusOrderByCreatedAtDesc(Integer status, Pageable pageable);

    Page<Article> findByStatusAndCategoryOrderByCreatedAtDesc(Integer status, String category, Pageable pageable);

    Page<Article> findByStatusAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(Integer status, String keyword, Pageable pageable);

    Page<Article> findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(String keyword, Pageable pageable);

    Page<Article> findAllByOrderByCreatedAtDesc(Pageable pageable);
}
