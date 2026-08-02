package com.unidata.uni.service;

import com.unidata.uni.config.GlobalExceptionHandler.BizException;
import com.unidata.uni.dto.PageResult;
import com.unidata.uni.dto.ScoreLineView;
import com.unidata.uni.entity.Activity;
import com.unidata.uni.entity.Article;
import com.unidata.uni.entity.NationalLine;
import com.unidata.uni.entity.School;
import com.unidata.uni.entity.ScoreLine;
import com.unidata.uni.entity.ScoreSource;
import com.unidata.uni.entity.User;
import com.unidata.uni.repository.ActivityRepository;
import com.unidata.uni.repository.ArticleRepository;
import com.unidata.uni.repository.NationalLineRepository;
import com.unidata.uni.repository.SchoolRepository;
import com.unidata.uni.repository.ScoreLineRepository;
import com.unidata.uni.repository.ScoreSourceRepository;
import com.unidata.uni.repository.UserRepository;
import com.unidata.uni.security.CurrentUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class PublicService {

    private final SchoolRepository schoolRepository;
    private final ScoreLineRepository scoreLineRepository;
    private final ArticleRepository articleRepository;
    private final ActivityRepository activityRepository;
    private final UserRepository userRepository;
    private final NationalLineRepository nationalLineRepository;
    private final ScoreSourceRepository scoreSourceRepository;

    public PublicService(SchoolRepository schoolRepository,
                         ScoreLineRepository scoreLineRepository,
                         ArticleRepository articleRepository,
                         ActivityRepository activityRepository,
                         UserRepository userRepository,
                         NationalLineRepository nationalLineRepository,
                         ScoreSourceRepository scoreSourceRepository) {
        this.schoolRepository = schoolRepository;
        this.scoreLineRepository = scoreLineRepository;
        this.articleRepository = articleRepository;
        this.activityRepository = activityRepository;
        this.userRepository = userRepository;
        this.nationalLineRepository = nationalLineRepository;
        this.scoreSourceRepository = scoreSourceRepository;
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
        List<ScoreLine> lines = scoreLineRepository.findBySchoolIdOrderByYearDesc(id);
        boolean hasRealLines = lines.stream().anyMatch(l -> "复试线".equals(l.getLineType()));
        List<ScoreLineView> views = lines.stream()
                .map(s -> ScoreLineView.from(s, vip))
                .toList();
        Map<String, Object> detail = new HashMap<>();
        detail.put("school", school);
        detail.put("scoreLines", views);
        detail.put("hasRealLines", hasRealLines);
        detail.put("engineeringSubjects", buildEngineeringSubjects(lines));
        detail.put("years", scoreLineRepository.findDistinctYears());
        detail.put("scoreSources", scoreSourceRepository.findBySchoolIdOrderByYearDescSortAsc(id));
        return detail;
    }

    /** 工学门类（08 代码）一级学科清单：0800 门类 + 38 个一级学科 */
    private static final String[][] ENGINEERING_SUBJECTS = {
            {"0800", "工学门类"},
            {"0801", "力学"},
            {"0802", "机械工程"},
            {"0803", "光学工程"},
            {"0804", "仪器科学与技术"},
            {"0805", "材料科学与工程"},
            {"0806", "冶金工程"},
            {"0807", "动力工程及工程热物理"},
            {"0808", "电气工程"},
            {"0809", "电子科学与技术"},
            {"0810", "信息与通信工程"},
            {"0811", "控制科学与工程"},
            {"0812", "计算机科学与技术"},
            {"0813", "建筑学"},
            {"0814", "土木工程"},
            {"0815", "水利工程"},
            {"0816", "测绘科学与技术"},
            {"0817", "化学工程与技术"},
            {"0818", "地质资源与地质工程"},
            {"0819", "矿业工程"},
            {"0820", "石油与天然气工程"},
            {"0821", "纺织科学与工程"},
            {"0822", "轻工技术与工程"},
            {"0823", "交通运输工程"},
            {"0824", "船舶与海洋工程"},
            {"0825", "航空宇航科学与技术"},
            {"0826", "兵器科学与技术"},
            {"0827", "核科学与技术"},
            {"0828", "农业工程"},
            {"0829", "林业工程"},
            {"0830", "环境科学与工程"},
            {"0831", "生物医学工程"},
            {"0832", "食品科学与工程"},
            {"0833", "城乡规划学"},
            {"0835", "软件工程"},
            {"0836", "生物工程"},
            {"0837", "安全科学与工程"},
            {"0838", "公安技术"},
            {"0839", "网络空间安全"},
    };

    /** 组装"工学一级学科覆盖情况"：每项列出 2025/2026 是否有该校真实复试线记录 */
    private List<Map<String, Object>> buildEngineeringSubjects(List<ScoreLine> lines) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (String[] s : ENGINEERING_SUBJECTS) {
            Map<String, Object> item = new HashMap<>();
            item.put("code", s[0]);
            item.put("name", s[1]);
            Map<Integer, ScoreLineView> byYear = new HashMap<>();
            for (ScoreLine l : lines) {
                if (!"复试线".equals(l.getLineType()) || l.getYear() == null) {
                    continue;
                }
                if (subjectMatches(l.getMajor(), s[0], s[1])) {
                    byYear.putIfAbsent(l.getYear(), ScoreLineView.from(l, true));
                }
            }
            item.put("lines", byYear);
            result.add(item);
        }
        return result;
    }

    private boolean subjectMatches(String major, String code, String name) {
        if (major == null) {
            return false;
        }
        if ("0800".equals(code)) {
            return major.contains("工学") || major.contains("其他学科专业");
        }
        if (major.contains(code)) {
            return true;
        }
        String kw = name.replace("科学与技术", "").replace("工程及工程热物理", "工程")
                .replace("石油与天然气", "油气").replace("地质资源与地质", "地质");
        return major.contains(kw);
    }

    public List<NationalLine> nationalLines(Integer year) {
        if (year != null) {
            return nationalLineRepository.findByYearOrderByIdAsc(year);
        }
        return nationalLineRepository.findAllByOrderByYearDescIdAsc();
    }

    public Map<String, Object> filters() {
        List<School> all = schoolRepository.findAll();
        Map<String, Object> filters = new HashMap<>();
        filters.put("provinces", all.stream().map(School::getProvince)
                .filter(Objects::nonNull).filter(s -> !s.isBlank())
                .distinct().sorted().toList());
        filters.put("categories", all.stream().map(School::getCategory)
                .filter(Objects::nonNull).filter(s -> !s.isBlank())
                .distinct().sorted().toList());
        filters.put("levels", all.stream().map(School::getLevel)
                .filter(Objects::nonNull).filter(s -> !s.isBlank())
                .distinct().sorted().toList());
        return filters;
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
            result = scoreLineRepository.findByMajorContainingIgnoreCaseOrRemarkContainingIgnoreCase(
                    major, major, pageable);
        } else {
            // 默认仅展示各校真实复试线，避免国家线参考记录刷屏
            result = scoreLineRepository.findByLineTypeNotOrderByCreatedAtDesc("国家线", pageable);
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
        List<School> hotSchools = schoolRepository.findByStatusOrderByLevelDescIdAsc(1, PageRequest.of(0, 12));
        List<Article> latestArticles = articleRepository
                .findByStatusOrderByCreatedAtDesc(1, PageRequest.of(0, 6)).getContent();
        List<Activity> latestActivities = activityRepository
                .findByStatusOrderByCreatedAtDesc(1, PageRequest.of(0, 4)).getContent();
        List<Integer> years = scoreLineRepository.findDistinctYears();
        List<ScoreLine> latestScoreLines = scoreLineRepository.findTop12ByLineTypeNotOrderByCreatedAtDesc("国家线");
        List<NationalLine> national2026 = nationalLineRepository.findByYearOrderByIdAsc(2026);

        // 34 所自划线院校（有官方复试线来源的院校）
        List<Long> sourceSchoolIds = scoreSourceRepository.findDistinctSchoolIds();
        List<School> selfLineSchools = sourceSchoolIds.isEmpty()
                ? List.of()
                : schoolRepository.findAllById(sourceSchoolIds);

        // 省份院校数量分布（取前 12）
        Map<String, Long> provinceCount = new HashMap<>();
        for (School s : schoolRepository.findAll()) {
            if (s.getProvince() != null && !s.getProvince().isBlank()) {
                provinceCount.merge(s.getProvince(), 1L, Long::sum);
            }
        }
        List<Map<String, Object>> provinceStats = provinceCount.entrySet().stream()
                .sorted((a, b) -> Long.compare(b.getValue(), a.getValue()))
                .limit(12)
                .map(e -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("province", e.getKey());
                    m.put("count", e.getValue());
                    return m;
                })
                .toList();

        Map<String, Object> home = new HashMap<>();
        home.put("hotSchools", hotSchools);
        home.put("latestArticles", latestArticles);
        home.put("latestActivities", latestActivities);
        home.put("latestScoreLines", latestScoreLines);
        home.put("national2026", national2026);
        home.put("selfLineSchools", selfLineSchools);
        home.put("provinceStats", provinceStats);
        home.put("years", years);
        home.put("schoolCount", schoolRepository.count());
        home.put("scoreLineCount", scoreLineRepository.count());
        home.put("nationalLineCount", nationalLineRepository.count());
        home.put("userCount", userRepository.count());
        home.put("stats", Map.of(
                "schools", schoolRepository.count(),
                "scoreLines", scoreLineRepository.count(),
                "nationalLines", nationalLineRepository.count(),
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
