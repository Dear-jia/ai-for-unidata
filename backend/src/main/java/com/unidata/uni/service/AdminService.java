package com.unidata.uni.service;

import com.unidata.uni.config.GlobalExceptionHandler.BizException;
import com.unidata.uni.dto.PageResult;
import com.unidata.uni.entity.Activity;
import com.unidata.uni.entity.Article;
import com.unidata.uni.entity.MemberOrder;
import com.unidata.uni.entity.School;
import com.unidata.uni.entity.ScoreLine;
import com.unidata.uni.entity.User;
import com.unidata.uni.repository.ActivityRepository;
import com.unidata.uni.repository.ArticleRepository;
import com.unidata.uni.repository.MemberOrderRepository;
import com.unidata.uni.repository.SchoolRepository;
import com.unidata.uni.repository.ScoreLineRepository;
import com.unidata.uni.repository.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final ScoreLineRepository scoreLineRepository;
    private final ArticleRepository articleRepository;
    private final ActivityRepository activityRepository;
    private final MemberOrderRepository orderRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminService(UserRepository userRepository,
                        SchoolRepository schoolRepository,
                        ScoreLineRepository scoreLineRepository,
                        ArticleRepository articleRepository,
                        ActivityRepository activityRepository,
                        MemberOrderRepository orderRepository,
                        PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.schoolRepository = schoolRepository;
        this.scoreLineRepository = scoreLineRepository;
        this.articleRepository = articleRepository;
        this.activityRepository = activityRepository;
        this.orderRepository = orderRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ---------- 用户管理 ----------

    public PageResult<User> users(String keyword, int page, int size) {
        if (keyword == null || keyword.isBlank()) {
            return PageResult.of(userRepository.findAll(PageRequest.of(page - 1, size)));
        }
        return PageResult.of(userRepository.findByUsernameContainingIgnoreCaseOrNicknameContainingIgnoreCase(
                keyword, keyword, PageRequest.of(page - 1, size)));
    }

    @Transactional
    public User updateUser(Long id, User patch) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BizException("用户不存在"));
        if (patch.getNickname() != null && !patch.getNickname().isBlank()) {
            user.setNickname(patch.getNickname());
        }
        if (patch.getEmail() != null) {
            user.setEmail(patch.getEmail());
        }
        if (patch.getPhone() != null) {
            user.setPhone(patch.getPhone());
        }
        if (patch.getStatus() != null) {
            user.setStatus(patch.getStatus());
        }
        if (patch.getRole() != null && (patch.getRole().equals("ADMIN") || patch.getRole().equals("USER"))) {
            user.setRole(patch.getRole());
        }
        if (patch.getMembershipType() != null && (patch.getMembershipType().equals("VIP") || patch.getMembershipType().equals("FREE"))) {
            user.setMembershipType(patch.getMembershipType());
        }
        if (patch.getMembershipExpireAt() != null) {
            user.setMembershipExpireAt(patch.getMembershipExpireAt());
        }
        if (patch.getPassword() != null && !patch.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(patch.getPassword()));
        }
        return user;
    }

    @Transactional
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BizException("用户不存在"));
        if ("ADMIN".equals(user.getRole())) {
            throw new BizException("不能删除管理员账号");
        }
        userRepository.delete(user);
    }

    @Transactional
    public void resetPassword(Long id, String newPassword) {
        if (newPassword == null || newPassword.length() < 6 || newPassword.length() > 32) {
            throw new BizException("新密码长度需在6-32位之间");
        }
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BizException("用户不存在"));
        user.setPassword(passwordEncoder.encode(newPassword));
    }

    // ---------- 学校管理 ----------

    public PageResult<School> schools(String keyword, int page, int size) {
        if (keyword == null || keyword.isBlank()) {
            return PageResult.of(schoolRepository.findAll(PageRequest.of(page - 1, size)));
        }
        return PageResult.of(schoolRepository.findByNameContainingIgnoreCase(keyword, PageRequest.of(page - 1, size)));
    }

    public School saveSchool(School school) {
        if (school.getName() == null || school.getName().isBlank()) {
            throw new BizException("学校名称不能为空");
        }
        return schoolRepository.save(school);
    }

    @Transactional
    public void deleteSchool(Long id) {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new BizException("学校不存在"));
        List<ScoreLine> lines = scoreLineRepository.findBySchoolIdOrderByYearDesc(id);
        scoreLineRepository.deleteAll(lines);
        schoolRepository.delete(school);
    }

    // ---------- 分数线管理 ----------

    public PageResult<ScoreLine> scoreLines(Long schoolId, Integer year, String major, int page, int size) {
        if (schoolId != null && year != null) {
            return PageResult.of(scoreLineRepository.findBySchoolIdAndYear(schoolId, year, PageRequest.of(page - 1, size)));
        }
        if (schoolId != null) {
            return PageResult.of(scoreLineRepository.findBySchoolId(schoolId, PageRequest.of(page - 1, size)));
        }
        if (major != null && !major.isBlank()) {
            return PageResult.of(scoreLineRepository.findByMajorContainingIgnoreCase(major, PageRequest.of(page - 1, size)));
        }
        return PageResult.of(scoreLineRepository.findAll(PageRequest.of(page - 1, size)));
    }

    @Transactional
    public ScoreLine saveScoreLine(ScoreLine line) {
        if (line.getSchoolId() == null) {
            throw new BizException("请选择学校");
        }
        School school = schoolRepository.findById(line.getSchoolId())
                .orElseThrow(() -> new BizException("学校不存在"));
        line.setSchoolName(school.getName());
        if (line.getYear() == null) {
            throw new BizException("请填写年份");
        }
        if (line.getMajor() == null || line.getMajor().isBlank()) {
            throw new BizException("请填写专业");
        }
        if (line.getPremium() == null) {
            line.setPremium(true);
        }
        return scoreLineRepository.save(line);
    }

    @Transactional
    public void deleteScoreLine(Long id) {
        scoreLineRepository.deleteById(id);
    }

    // ---------- 资讯管理 ----------

    public PageResult<Article> articles(String keyword, int page, int size) {
        if (keyword == null || keyword.isBlank()) {
            return PageResult.of(articleRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page - 1, size)));
        }
        return PageResult.of(articleRepository.findByTitleContainingIgnoreCaseOrderByCreatedAtDesc(
                keyword, PageRequest.of(page - 1, size)));
    }

    public Article saveArticle(Article article) {
        if (article.getTitle() == null || article.getTitle().isBlank()) {
            throw new BizException("标题不能为空");
        }
        if (article.getStatus() == null) {
            article.setStatus(1);
        }
        if (article.getViews() == null) {
            article.setViews(0L);
        }
        return articleRepository.save(article);
    }

    public void deleteArticle(Long id) {
        articleRepository.deleteById(id);
    }

    // ---------- 活动管理 ----------

    public PageResult<Activity> activities(int page, int size) {
        return PageResult.of(activityRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(page - 1, size)));
    }

    public Activity saveActivity(Activity activity) {
        if (activity.getTitle() == null || activity.getTitle().isBlank()) {
            throw new BizException("活动标题不能为空");
        }
        if (activity.getStatus() == null) {
            activity.setStatus(1);
        }
        return activityRepository.save(activity);
    }

    public void deleteActivity(Long id) {
        activityRepository.deleteById(id);
    }

    // ---------- 统计 ----------

    public Map<String, Object> stats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("users", userRepository.count());
        stats.put("schools", schoolRepository.count());
        stats.put("scoreLines", scoreLineRepository.count());
        stats.put("articles", articleRepository.count());
        stats.put("activities", activityRepository.count());
        stats.put("orders", orderRepository.count());
        long vipCount = userRepository.findAll().stream().filter(User::isVip).count();
        stats.put("vipUsers", vipCount);
        stats.put("paidAmount", orderRepository.findAll().stream()
                .filter(o -> "PAID".equals(o.getStatus()))
                .mapToDouble(o -> o.getAmount().doubleValue())
                .sum());
        return stats;
    }

    public Map<String, Object> overview() {
        Map<String, Object> map = new HashMap<>();
        map.put("recentUsers", userRepository.findAll(PageRequest.of(0, 5)));
        map.put("recentOrders", orderRepository.findTop5ByOrderByCreatedAtDesc());
        map.put("recentArticles", articleRepository.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 5)).getContent());
        return map;
    }
}
