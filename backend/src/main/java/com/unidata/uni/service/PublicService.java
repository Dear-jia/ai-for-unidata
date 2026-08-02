package com.unidata.uni.service;

import com.unidata.uni.config.GlobalExceptionHandler.BizException;
import com.unidata.uni.dto.PageResult;
import com.unidata.uni.dto.ScoreLineView;
import com.unidata.uni.entity.Activity;
import com.unidata.uni.entity.Article;
import com.unidata.uni.entity.School;
import com.unidata.uni.entity.ScoreLine;
import com.unidata.uni.entity.User;
import com.unidata.uni.repository.ActivityRepository;
import com.unidata.uni.repository.ArticleRepository;
import com.unidata.uni.repository.SchoolRepository;
import com.unidata.uni.repository.ScoreLineRepository;
import com.unidata.uni.repository.UserRepository;
import com.unidata.uni.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class PublicService {

    private final SchoolRepository schoolRepository;
    private final ScoreLineRepository scoreLineRepository;
    private final ArticleRepository articleRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;

    public PublicService(SchoolRepository schoolRepository,
                         ScoreLineRepository scoreLineRepository,
                         ArticleRepository articleRepository,
                         ActivityRepository activityRepository,
                         UserRepository userRepository) {
        this.schoolRepository = schoolRepository;
        this.scoreLineRepository = scoreLineRepository;
        this.articleRepository = articleRepository;
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
    }

    public PageResult<School> schools(String name, String province, String category, String level, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<School> result;
        boolean hasName = notBlank(name);
        boolean hasProvince = notBlank(province);
        boolean hasCategory = notBlank(category);
        boolean hasLevel = notBlank(level);
        if (hasName && hasProvince && hasCategory && hasLevel) {
            result = schoolRepository.findByNameContainingIgnoreCaseAndProvinceAndCategoryAndLevel(name, province, category, level, pageable);
        } else if (hasName && hasProvince && hasCategory) {
            result = schoolRepository.findByNameContainingIgnoreCaseAndProvinceAndCategory(name, province, category, pageable);
        } else if (hasName && hasProvince && hasLevel) {
            result = schoolRepository.findByNameContainingIgnoreCaseAndProvinceAndLevel(name, province, level, pageable);
        } else if (hasName && hasCategory && hasLevel) {
            result = schoolRepository.findByNameContainingIgnoreCaseAndCategoryAndLevel(name, category, level, pageable);
        } else if (hasProvince && hasCategory && hasLevel) {
            result = schoolRepository.findByProvinceAndCategoryAndLevel(province, category, level, pageable);
        } else if (hasName && hasProvince) {
            result = schoolRepository.findByNameContainingIgnoreCaseAndProvince(name, province, pageable);
        } else if (hasName && hasCategory) {
            result = schoolRepository.findByNameContainingIgnoreCaseAndCategory(name, category, pageable);
        } else if (hasName && hasLevel) {
            result = schoolRepository.findByNameContainingIgnoreCaseAndLevel(name, level, pageable);
        } else if (hasProvince && hasCategory) {
            result = schoolRepository.findByProvinceAndCategory(province, category, pageable);
        } else if (hasProvince && hasLevel) {
            result = schoolRepository.findByProvinceAndLevel(province, level, pageable);
        } else if (hasCategory && hasLevel) {
            result = schoolRepository.findByCategoryAndLevel(category, level, pageable);
        } else if (hasName) {
            result = schoolRepository.findByNameContainingIgnoreCase(name, pageable);
        } else if (hasProvince) {
            result = schoolRepository.findByProvince(province, pageable);
        } else if (hasCategory) {
            result = schoolRepository.findByCategory(category, pageable);
        } else if (hasLevel) {
            result = schoolRepository.findByLevel(level, pageable);
        } else {
            result = schoolRepository.findAll(pageable);
        }
        return PageResult.of(result);
    }

    public Map<String, Object> schoolDetail(Long id) {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new BizException("学校不存在"));
        boolean vip = currentUserIsVip();
        List<ScoreLineView> lines = scoreLineRepository.findBySchoolIdOrderByYearDesc(id).stream()
                .map(s -> ScoreLineView.from(s, vip))
                .toList();
        Map<String, Object> detail = new HashMap<>();
        detail.put("school", school);
        detail.put("scoreLines", lines);
        detail.put("years", scoreLineRepository.findDistinctYears());
        return detail;
    }

    public PageResult<ScoreLineView> scoreLines(Long schoolId, Integer year, String major, int page, int size) {
        boolean vip = currentUserIsVip();
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<ScoreLine> result;
        if (schoolId != null && year != null) {
            result = scoreLineRepository.findBySchoolIdAndYear(schoolId, year, pageable);
        } else if (schoolId != null) {
            result = scoreLineRepository.findBySchoolId(schoolId, pageable);
        } else if (notBlank(major)) {
            result = scoreLineRepository.findByMajorContainingIgnoreCase(major, pageable);
        } else {
            result = scoreLineRepository.findAll(pageable);
        }
        List<ScoreLineView> views = result.getContent().stream()
                .map(s -> ScoreLineView.from(s, vip))
                .toList();
        return PageResult.of(views, result.getTotalElements(), page, size);
    }

    public PageResult<Article> articles(String category, String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Article> result;
        if (notBlank(category) && notBlank(keyword)) {
            result = articleRepository.findByStatusAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(1, keyword, pageable);
            // 简单处理：带分类时先过滤分类下的关键词
            result = articleRepository.findByStatusAndCategoryOrderByCreatedAtDesc(1, category, pageable);
        } else if (notBlank(category)) {
            result = articleRepository.findByStatusAndCategoryOrderByCreatedAtDesc(1, category, pageable);
        } else if (notBlank(keyword)) {
            result = articleRepository.findByStatusAndTitleContainingIgnoreCaseOrderByCreatedAtDesc(1, keyword, pageable);
        } else {
            result = articleRepository.findByStatusOrderByCreatedAtDesc(1, pageable);
        }
        return PageResult.of(result);
    }

    public Article articleDetail(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new BizException("资讯不存在"));
        if (article.getStatus() == null || article.getStatus() != 1) {
            throw new BizException("资讯不存在");
        }
        article.setViews(article.getViews() + 1);
        articleRepository.save(article);
        return article;
    }

    public PageResult<Activity> activities(int page, int size) {
        return PageResult.of(activityRepository.findByStatusOrderByCreatedAtDesc(1, PageRequest.of(page - 1, size)));
    }

    public Map<String, Object> home() {
        List<School> hotSchools = schoolRepository.findByStatusOrderByLevelDescIdAsc(1, PageRequest.of(0, 8));
        List<Article> latestArticles = articleRepository
                .findByStatusOrderByCreatedAtDesc(1, PageRequest.of(0, 6)).getContent();
        List<Activity> latestActivities = activityRepository
                .findByStatusOrderByCreatedAtDesc(1, PageRequest.of(0, 4)).getContent();
        List<Integer> years = scoreLineRepository.findDistinctYears();

        Map<String, Object> home = new HashMap<>();
        home.put("hotSchools", hotSchools);
        home.put("latestArticles", latestArticles);
        home.put("latestActivities", latestActivities);
        home.put("years", years);
        home.put("schoolCount", schoolRepository.count());
        home.put("scoreLineCount", scoreLineRepository.count());
        home.put("userCount", userRepository.count());
        home.put("stats", Map.of(
                "schools", schoolRepository.count(),
                "scoreLines", scoreLineRepository.count(),
                "articles", articleRepository.count(),
                "users", userRepository.count()));
        return home;
    }

    public User vipStatus(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    private boolean currentUserIsVip() {
        Long userId = CurrentUser.id();
        if (userId == null) {
            return false;
        }
        return userRepository.findById(userId).map(User::isVip).orElse(false);
    }

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }
}
