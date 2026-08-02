package com.unidata.uni.config;

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
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final ScoreLineRepository scoreLineRepository;
    private final NationalLineRepository nationalLineRepository;
    private final ScoreSourceRepository scoreSourceRepository;
    private final ArticleRepository articleRepository;
    private final ActivityRepository activityRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      SchoolRepository schoolRepository,
                      ScoreLineRepository scoreLineRepository,
                      NationalLineRepository nationalLineRepository,
                      ScoreSourceRepository scoreSourceRepository,
                      ArticleRepository articleRepository,
                      ActivityRepository activityRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.schoolRepository = schoolRepository;
        this.scoreLineRepository = scoreLineRepository;
        this.nationalLineRepository = nationalLineRepository;
        this.scoreSourceRepository = scoreSourceRepository;
        this.articleRepository = articleRepository;
        this.activityRepository = activityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        log.info("开始检查并初始化数据...");
        makeAllScoreLinesFree();
        removeOldScoreLines();
        seedUsers();
        seedSchoolsFromCsv();
        seedSchoolTextLines();
        seedSchoolNationalReference();
        seedNationalLines();
        seedSchoolScoreSources();
        seedArticles();
        seedActivities();
        log.info("数据检查完成。管理员账号 admin / admin123，院校 {} 所，分数线 {} 条，国家线 {} 条",
                schoolRepository.count(), scoreLineRepository.count(), nationalLineRepository.count());
    }

    // ---------- 用户 ----------

    /** 会员特权暂未上线：将全部分数线数据设为免费公开（幂等，每次启动执行） */
    private void makeAllScoreLinesFree() {
        List<ScoreLine> lines = scoreLineRepository.findAll();
        boolean changed = false;
        for (ScoreLine line : lines) {
            if (Boolean.TRUE.equals(line.getPremium())) {
                line.setPremium(false);
                changed = true;
            }
        }
        if (changed) {
            scoreLineRepository.saveAll(lines);
            log.info("已将所有分数线数据设为免费公开（共 {} 条）", lines.size());
        }
    }

    /** 仅保留 2025、2026 两年数据：清理更早的分数线与国家线（幂等，每次启动执行） */
    private void removeOldScoreLines() {
        long lines = scoreLineRepository.deleteByYearLessThan(2025);
        long nationals = nationalLineRepository.deleteByYearLessThan(2025);
        if (lines > 0 || nationals > 0) {
            log.info("已清理 2024 及更早数据：院校分数线 {} 条，国家线 {} 条", lines, nationals);
        }
    }

    private void seedUsers() {
        if (userRepository.count() > 0) {
            return;
        }
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword(passwordEncoder.encode("admin123"));
        admin.setNickname("平台管理员");
        admin.setRole("ADMIN");
        userRepository.save(admin);

        User vip = new User();
        vip.setUsername("demo");
        vip.setPassword(passwordEncoder.encode("demo123"));
        vip.setNickname("演示会员");
        vip.setMembershipType("VIP");
        vip.setMembershipExpireAt(LocalDateTime.now().plusYears(1));
        userRepository.save(vip);
    }

    // ---------- 学校库（939 所招生单位，来自研招网院校库） ----------

    private void seedSchoolsFromCsv() {
        if (schoolRepository.count() >= 900) {
            log.info("院校数据已存在（{} 所），跳过导入", schoolRepository.count());
            return;
        }
        int created = 0;
        int updated = 0;
        try (Reader reader = new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream("/schools.csv")), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {
            for (CSVRecord rec : parser) {
                String name = rec.get("name");
                if (name == null || name.isBlank()) {
                    continue;
                }
                name = name.trim();
                Optional<School> exist = schoolRepository.findFirstByName(name);
                School s = exist.orElseGet(School::new);
                s.setName(name);
                s.setProvince(clean(rec.get("province")));
                s.setDept(clean(rec.get("dept")));
                s.setLevel(clean(rec.get("level")));
                s.setAdmissionUrl(clean(rec.get("admissionUrl")));
                if (s.getCategory() == null || s.getCategory().isBlank()) {
                    s.setCategory(guessCategory(name));
                }
                if (s.getStatus() == null) {
                    s.setStatus(1);
                }
                schoolRepository.save(s);
                if (exist.isEmpty()) created++; else updated++;
            }
        } catch (Exception e) {
            log.error("导入 schools.csv 失败", e);
        }
        log.info("院校库导入完成：新增 {} 所，更新 {} 所，当前共 {} 所", created, updated, schoolRepository.count());
    }

    private String guessCategory(String name) {
        String n = name == null ? "" : name;
        if (n.contains("师范") || n.contains("教育")) return "师范";
        if (n.contains("民族")) return "民族";
        if (n.contains("医药") || n.contains("医科") || n.contains("中医") || n.contains("医学")
                || n.contains("卫生") || n.contains("药科") || n.contains("护理")) return "医药";
        if (n.contains("财经") || n.contains("金融") || n.contains("经贸") || n.contains("工商")
                || n.contains("商业") || n.contains("审计") || n.contains("税务") || n.contains("经济")) return "财经";
        if (n.contains("政法") || n.contains("公安") || n.contains("警察") || n.contains("司法")
                || n.contains("国际关系") || n.contains("外交")) return "政法";
        if (n.contains("艺术") || n.contains("美术") || n.contains("音乐") || n.contains("传媒")
                || n.contains("戏剧") || n.contains("电影") || n.contains("舞蹈") || n.contains("设计")) return "艺术";
        if (n.contains("语言") || n.contains("外国语") || n.contains("外事")) return "语言";
        if (n.contains("农业") || n.contains("农林") || n.contains("林业") || n.contains("畜牧")
                || n.contains("水产") || n.contains("农垦")) return "农林";
        if (n.contains("体育")) return "体育";
        if (n.contains("军事") || n.contains("陆军") || n.contains("海军") || n.contains("空军")
                || n.contains("国防大学") || n.contains("警官")) return "军事";
        if (n.contains("理工") || n.contains("科技") || n.contains("工业") || n.contains("交通")
                || n.contains("航空") || n.contains("航天") || n.contains("电力") || n.contains("邮电")
                || n.contains("电子") || n.contains("石油") || n.contains("地质") || n.contains("矿业")
                || n.contains("冶金") || n.contains("化工") || n.contains("建筑") || n.contains("水利")
                || n.contains("海洋") || n.contains("船舶") || n.contains("国防") || n.contains("信息")
                || n.contains("工程") || n.contains("机电") || n.contains("铁道")) return "理工";
        return "综合";
    }

    // ---------- 34 校中可解析为文本的复试线（如厦门大学 2025） ----------

    private void seedSchoolTextLines() {
        List<SchoolTextRow> rows = readSchoolTextLines();
        if (rows.isEmpty()) {
            return;
        }
        List<ScoreLine> pending = new ArrayList<>();
        java.util.Set<String> handled = new java.util.HashSet<>();
        for (SchoolTextRow row : rows) {
            School school = schoolRepository.findFirstByName(row.school).orElse(null);
            if (school == null) continue;
            String key = school.getId() + "-" + row.year;
            if (handled.add(key)) {
                scoreLineRepository.deleteBySchoolIdAndYearAndLineType(school.getId(), row.year, "复试线");
            }
            ScoreLine line = new ScoreLine();
            line.setSchoolId(school.getId());
            line.setSchoolName(school.getName());
            line.setYear(row.year);
            line.setMajor(row.discipline);
            line.setLineType("复试线");
            line.setMinScore(row.total);
            line.setPoliticalScore(row.oneHundred);
            line.setMajorScore1(row.overHundred);
            line.setPremium(false);
            line.setRemark(row.note == null || row.note.isBlank() ? "官方公布的复试基本分数线（研招网）" : row.note);
            pending.add(line);
        }
        scoreLineRepository.saveAll(pending);
        if (!pending.isEmpty()) {
            log.info("院校文本复试线已写入 {} 条", pending.size());
        }
    }

    /** 每所院校补充全学科门类国家线参考记录（2025/2026），保证院校详情页与厦门大学一样具备完整学科数据 */
    private void seedSchoolNationalReference() {
        // 重建式写入：先清除所有"国家线参考"记录（仅限平台生成的参考行，不影响各校真实复试线）
        long deleted = scoreLineRepository.deleteByLineTypeAndRemarkContaining("国家线", "国家线参考");
        List<NationalLine> refs = nationalLineRepository.findAllByOrderByYearDescIdAsc();
        List<ScoreLine> pending = new ArrayList<>();
        for (School school : schoolRepository.findAll()) {
            for (NationalLine nl : refs) {
                if (nl.getYear() == null || nl.getYear() < 2025) {
                    continue;
                }
                if (nl.getDiscipline() == null || nl.getDiscipline().isBlank()) {
                    continue;
                }
                ScoreLine line = new ScoreLine();
                line.setSchoolId(school.getId());
                line.setSchoolName(school.getName());
                line.setYear(nl.getYear());
                line.setMajor(nl.getDiscipline());
                line.setLineType("国家线");
                line.setMinScore(nl.getTotalA());
                line.setPoliticalScore(nl.getOneA());
                line.setForeignScore(nl.getOverA());
                line.setPremium(false);
                boolean isComputerEngineering = nl.getDiscipline().startsWith("工学")
                        && nl.getDiscipline().contains("其他");
                String remark = isComputerEngineering
                        ? "计算机相关学科（工学门类）A类国家线参考，以官方公布为准"
                        : "国家线参考，以官方公布为准";
                if (nl.getSubjects() != null && !nl.getSubjects().isBlank()
                        && !"各学科专业".equals(nl.getSubjects())) {
                    remark += "（" + nl.getSubjects() + "）";
                }
                line.setRemark(remark);
                pending.add(line);
            }
        }
        if (!pending.isEmpty()) {
            scoreLineRepository.saveAll(pending);
        }
        log.info("院校全学科国家线参考重建完成：删除 {} 条，写入 {} 条（{} 所院校 × 2 年）",
                deleted, pending.size(), schoolRepository.count());
    }

    private List<SchoolTextRow> readSchoolTextLines() {
        List<SchoolTextRow> rows = new ArrayList<>();
        try (Reader reader = new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream("/school-score-lines.csv")), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {
            for (CSVRecord rec : parser) {
                SchoolTextRow row = new SchoolTextRow();
                row.school = clean(rec.get("school"));
                row.year = parseInt(rec.get("year"));
                row.discipline = clean(rec.get("discipline"));
                row.subjects = clean(rec.get("subjects"));
                row.oneHundred = parseInt(rec.get("oneHundred"));
                row.overHundred = parseInt(rec.get("overHundred"));
                row.total = parseInt(rec.get("total"));
                row.note = clean(rec.get("note"));
                if (row.school != null && row.year != null && row.total != null) {
                    rows.add(row);
                }
            }
        } catch (Exception e) {
            log.error("读取 school-score-lines.csv 失败", e);
        }
        return rows;
    }

    // ---------- 国家线 ----------

    private void seedNationalLines() {
        if (nationalLineRepository.count() > 0) {
            return;
        }
        int saved = 0;
        try (Reader reader = new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream("/national-lines.csv")), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {
            for (CSVRecord rec : parser) {
                NationalLine line = new NationalLine();
                line.setYear(parseInt(rec.get("year")));
                line.setDiscipline(clean(rec.get("discipline")));
                line.setSubjects(clean(rec.get("subjects")));
                line.setTotalA(parseInt(rec.get("totalA")));
                line.setOneA(parseInt(rec.get("oneA")));
                line.setOverA(parseInt(rec.get("overA")));
                line.setTotalB(parseInt(rec.get("totalB")));
                line.setOneB(parseInt(rec.get("oneB")));
                line.setOverB(parseInt(rec.get("overB")));
                line.setNote(clean(rec.get("note")));
                if (line.getYear() != null && line.getDiscipline() != null) {
                    nationalLineRepository.save(line);
                    saved++;
                }
            }
        } catch (Exception e) {
            log.error("读取 national-lines.csv 失败", e);
        }
        log.info("国家线已写入 {} 条（2024-2026）", saved);
    }

    // ---------- 34 校官方复试线图片来源 ----------

    private void seedSchoolScoreSources() {
        if (scoreSourceRepository.count() > 0) {
            return;
        }
        int saved = 0;
        try (Reader reader = new InputStreamReader(
                Objects.requireNonNull(getClass().getResourceAsStream("/school-score-sources.csv")), StandardCharsets.UTF_8);
             CSVParser parser = CSVFormat.DEFAULT.builder()
                     .setHeader()
                     .setSkipHeaderRecord(true)
                     .setTrim(true)
                     .build()
                     .parse(reader)) {
            int sort = 0;
            for (CSVRecord rec : parser) {
                String schoolName = clean(rec.get("school"));
                School school = schoolName == null ? null : schoolRepository.findFirstByName(schoolName).orElse(null);
                if (school == null) continue;
                ScoreSource src = new ScoreSource();
                src.setSchoolId(school.getId());
                src.setSchoolName(school.getName());
                src.setYear(parseInt(rec.get("year")));
                src.setTitle(clean(rec.get("title")));
                src.setImageUrl(clean(rec.get("imageUrl")));
                src.setSourceUrl(clean(rec.get("sourceUrl")));
                src.setSort(sort++);
                scoreSourceRepository.save(src);
                saved++;
            }
        } catch (Exception e) {
            log.error("读取 school-score-sources.csv 失败", e);
        }
        log.info("34 校官方复试线图片来源已写入 {} 条", saved);
    }

    // ---------- 资讯 / 活动 ----------

    private void seedArticles() {
        if (articleRepository.count() > 0) {
            return;
        }
        List<Article> articles = new ArrayList<>();
        articles.add(article("2026考研时间线全攻略：从报名到复试的每个节点",
                "政策解读",
                "为你梳理考研全程关键时间节点，避免错过任何重要环节。",
                "<p>考研是一场持久战，明确每个阶段的任务至关重要。</p><h3>一、准备阶段（现在-9月）</h3><p>确定目标院校与专业，收集历年分数线、报录比等数据。</p><h3>二、报名阶段（9-10月）</h3><p>网上报名、确认信息，注意每个省的具体要求。</p><h3>三、初试阶段（12月）</h3><p>全国统一初试，考前一天踩点，带齐证件。</p><h3>四、复试调剂（次年3-4月）</h3><p>初试出分后及时准备复试，未达线可关注调剂信息。</p>"));
        articles.add(article("2026年国家线正式公布：多学科分数线一览",
                "资讯",
                "教育部公布 2026 年全国硕士研究生招生考试考生进入复试的初试成绩基本要求。",
                "<p>2026 年国家线已经公布，哲学 326 分、经济学 324 分、法学 321 分、文学 354 分、工学 264 分等。</p><p>管理类联考各专业继续单列分数线，工商管理 146 分、公共管理 168 分。</p><p>完整 A/B 类分数线可在本站「分数线」-「国家线」页面查看。</p>"));
        articles.add(article("上岸学姐经验谈：双非逆袭985的完整备考计划",
                "备考经验",
                "从择校到复试，一份可复制的逆袭路线图。",
                "<p>我的本科是一所普通双非院校，最终以初试第二的成绩上岸985。</p><p>关键不是拼时长，而是拼效率：<br>1. 数学每天保证4小时专注；<br>2. 英语坚持精读外刊；<br>3. 专业课建立思维导图反复过三轮。</p>"));
        articles.add(article("计算机考研院校梯度推荐：从冲一冲到保底",
                "院校解读",
                "按难度梯度整理热门计算机院校，附历年复试线。",
                "<p>计算机是考研最热门的专业之一，择校更要讲究梯度。</p><p>第一梯度（冲）：清华、北大、浙大<br>第二梯度（稳）：华科、武大、中大<br>第三梯度（保）：双一流特色院校与老牌工科强校</p><p>更多院校历年分数线可以在本平台会员专区查看。</p>"));
        articles.add(article("专硕 vs 学硕：怎么选才不会后悔？",
                "资讯",
                "从培养模式、就业方向、考试科目三个维度全面对比。",
                "<p>学硕重学术研究，专硕重实践应用，两者没有绝对优劣。</p><p>如果你有读博打算，学硕是更顺的路径；如果目标是快速就业，专硕学制短、更贴近产业需求。</p><p>同时注意：部分学校专硕学制已延长至3年，报考前务必看清招生简章。</p>"));
        articles.add(article("考研复试常见问题TOP10及应答模板",
                "备考经验",
                "把复试高频问题提前演练一遍，面试不再紧张。",
                "<p>复试问题千变万化，但高频问题是有规律的：</p><p>1. 请做自我介绍<br>2. 为什么报考我们学校<br>3. 研究生期间有什么规划<br>4. 最近读过哪些专业书籍<br>5. 如何看待你的本科成绩</p><p>准备时注意结合自身真实经历，切忌背稿痕迹过重。</p>"));
        articleRepository.saveAll(articles);
    }

    private Article article(String title, String category, String summary, String content) {
        Article a = new Article();
        a.setTitle(title);
        a.setCategory(category);
        a.setSummary(summary);
        a.setContent(content);
        a.setStatus(1);
        a.setViews((long) (Math.random() * 800) + 120);
        return a;
    }

    private void seedActivities() {
        if (activityRepository.count() > 0) {
            return;
        }
        List<Activity> activities = new ArrayList<>();
        activities.add(activity("新用户注册即送 3 天会员体验",
                "即日起注册平台的用户，可免费领取3天全站数据会员，畅享各大高校历年分数线。",
                LocalDateTime.now().minusDays(2), LocalDateTime.now().plusMonths(1)));
        activities.add(activity("暑期拼团季：三人成团享 8 折",
                "暑期备考黄金期，邀请两位研友一起开通年卡会员，三人均可享受 8 折优惠。",
                LocalDateTime.now().minusDays(10), LocalDateTime.now().plusMonths(2)));
        activities.add(activity("评论区晒分活动：晒出你的录取通知书",
                "在资讯评论区晒出你的录取通知书照片，即有机会获得全年免费会员！",
                LocalDateTime.now().plusDays(5), LocalDateTime.now().plusMonths(3)));
        activityRepository.saveAll(activities);
    }

    private Activity activity(String title, String content, LocalDateTime start, LocalDateTime end) {
        Activity a = new Activity();
        a.setTitle(title);
        a.setContent(content);
        a.setStartTime(start);
        a.setEndTime(end);
        a.setStatus(1);
        return a;
    }

    // ---------- 工具 ----------

    private String clean(String v) {
        if (v == null) return null;
        String s = v.trim();
        return s.isEmpty() ? null : s;
    }

    private Integer parseInt(String v) {
        if (v == null || v.isBlank()) return null;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static class SchoolTextRow {
        String school;
        Integer year;
        String discipline;
        String subjects;
        Integer oneHundred;
        Integer overHundred;
        Integer total;
        String note;
    }
}
