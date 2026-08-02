package com.unidata.uni.config;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final UserRepository userRepository;
    private final SchoolRepository schoolRepository;
    private final ScoreLineRepository scoreLineRepository;
    private final ArticleRepository articleRepository;
    private final ActivityRepository activityRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository,
                      SchoolRepository schoolRepository,
                      ScoreLineRepository scoreLineRepository,
                      ArticleRepository articleRepository,
                      ActivityRepository activityRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.schoolRepository = schoolRepository;
        this.scoreLineRepository = scoreLineRepository;
        this.articleRepository = articleRepository;
        this.activityRepository = activityRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }
        log.info("数据库为空，开始初始化演示数据...");
        seedUsers();
        seedSchoolsAndScores();
        seedArticles();
        seedActivities();
        log.info("演示数据初始化完成。管理员账号 admin / admin123");
    }

    private void seedUsers() {
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

    private void seedSchoolsAndScores() {
        List<School> schools = new ArrayList<>();
        schools.add(school("清华大学", "北京", "北京", "综合", "985/211/双一流",
                "国内顶尖综合性研究型大学，工科实力全球领先，计算机、电子、经管等专业报考热度极高。"));
        schools.add(school("北京大学", "北京", "北京", "综合", "985/211/双一流",
                "国内历史最悠久的高等学府之一，文理基础学科实力雄厚，光华、元培等学院报考竞争激烈。"));
        schools.add(school("复旦大学", "上海", "上海", "综合", "985/211/双一流",
                "地处上海的综合性研究型大学，新闻、金融、医学等专业位居全国前列。"));
        schools.add(school("浙江大学", "浙江", "杭州", "综合", "985/211/双一流",
                "学科门类齐全的综合性大学，计算机、控制、农学等专业实力突出。"));
        schools.add(school("武汉大学", "湖北", "武汉", "综合", "985/211/双一流",
                "风景优美的百年名校，法学、测绘、图书情报等专业特色鲜明。"));
        schools.add(school("上海交通大学", "上海", "上海", "综合", "985/211/双一流",
                "工科与医学并重的顶尖高校，安泰经管、电院、医学院报考热度常年居高。"));
        schools.add(school("华中科技大学", "湖北", "武汉", "理工", "985/211/双一流",
                "以工科见长的全国重点大学，光电、机械、临床医学等专业实力强劲。"));
        schools.add(school("中山大学", "广东", "广州", "综合", "985/211/双一流",
                "华南地区顶尖综合性大学，医学、经管、海洋科学等专业优势明显。"));
        schools.add(school("四川大学", "四川", "成都", "综合", "985/211/双一流",
                "西南地区综合实力最强的大学之一，口腔医学全国第一，材料、生物等专业优势突出。"));
        schools.add(school("华东师范大学", "上海", "上海", "师范", "985/211/双一流",
                "教育部直属重点师范大学，教育学、心理学、统计学等专业全国领先。"));
        schoolRepository.saveAll(schools);

        int[][] scores = {
                // 清华
                {2024, 385, 60, 60, 90, 90}, {2023, 380, 60, 60, 90, 90},
                {2022, 375, 55, 55, 90, 90}, {2021, 370, 55, 55, 85, 85},
                // 北大
                {2024, 390, 60, 60, 95, 95}, {2023, 385, 60, 60, 90, 90},
                {2022, 378, 55, 55, 90, 90}, {2021, 372, 55, 55, 85, 85},
                // 复旦
                {2024, 375, 60, 60, 90, 90}, {2023, 370, 60, 60, 90, 90},
                {2022, 365, 55, 55, 85, 85}, {2021, 360, 55, 55, 85, 85},
                // 浙大
                {2024, 380, 60, 60, 95, 95}, {2023, 375, 55, 55, 90, 90},
                {2022, 370, 55, 55, 90, 90}, {2021, 365, 55, 55, 85, 85},
                // 武大
                {2024, 365, 55, 55, 90, 90}, {2023, 360, 55, 55, 85, 85},
                {2022, 355, 50, 50, 85, 85}, {2021, 350, 50, 50, 80, 80},
                // 上交
                {2024, 383, 60, 60, 95, 95}, {2023, 377, 60, 60, 90, 90},
                {2022, 372, 55, 55, 90, 90}, {2021, 365, 55, 55, 85, 85},
                // 华科
                {2024, 360, 55, 55, 90, 90}, {2023, 355, 55, 55, 85, 85},
                {2022, 350, 50, 50, 85, 85}, {2021, 345, 50, 50, 80, 80},
                // 中大
                {2024, 358, 55, 55, 85, 85}, {2023, 352, 50, 50, 85, 85},
                {2022, 348, 50, 50, 80, 80}, {2021, 342, 50, 50, 80, 80},
                // 川大
                {2024, 355, 55, 55, 85, 85}, {2023, 350, 50, 50, 85, 85},
                {2022, 345, 50, 50, 80, 80}, {2021, 340, 50, 50, 80, 80},
                // 华师
                {2024, 368, 55, 55, 90, 90}, {2023, 362, 55, 55, 85, 85},
                {2022, 356, 50, 50, 85, 85}, {2021, 350, 50, 50, 80, 80},
        };
        String[] majors = {"计算机科学与技术", "软件工程", "电子信息", "会计学", "教育学", "心理学"};
        List<ScoreLine> lines = new ArrayList<>();
        for (int i = 0; i < schools.size(); i++) {
            School s = schools.get(i);
            int base = i * 4;
            for (int m = 0; m < 2; m++) {
                int[] row = scores[base + m];
                ScoreLine line = new ScoreLine();
                line.setSchoolId(s.getId());
                line.setSchoolName(s.getName());
                line.setYear(row[0]);
                line.setMajor(majors[(i + m) % majors.length]);
                line.setLineType("复试线");
                line.setMinScore(row[1]);
                line.setPoliticalScore(row[2]);
                line.setForeignScore(row[3]);
                line.setMajorScore1(row[4]);
                line.setMajorScore2(row[5]);
                line.setPremium(true);
                line.setRemark("该专业为历年热门专业，复试竞争较激烈");
                lines.add(line);
            }
        }
        // 再补充几条非会员可见的公开数据
        for (int i = 0; i < 4; i++) {
            ScoreLine line = new ScoreLine();
            line.setSchoolId(schools.get(i).getId());
            line.setSchoolName(schools.get(i).getName());
            line.setYear(2024);
            line.setMajor("马克思主义理论");
            line.setLineType("国家线");
            line.setMinScore(331 + i * 2);
            line.setPoliticalScore(45);
            line.setForeignScore(45);
            line.setMajorScore1(68);
            line.setMajorScore2(68);
            line.setPremium(false);
            line.setRemark("2024年国家线公开数据");
            lines.add(line);
        }
        scoreLineRepository.saveAll(lines);
    }

    private School school(String name, String province, String city, String category, String level, String intro) {
        School s = new School();
        s.setName(name);
        s.setProvince(province);
        s.setCity(city);
        s.setCategory(category);
        s.setLevel(level);
        s.setIntro(intro);
        s.setStatus(1);
        return s;
    }

    private void seedArticles() {
        List<Article> articles = new ArrayList<>();
        articles.add(article("2026考研时间线全攻略：从报名到复试的每个节点",
                "政策解读",
                "为你梳理考研全程关键时间节点，避免错过任何重要环节。",
                "<p>考研是一场持久战，明确每个阶段的任务至关重要。</p><h3>一、准备阶段（现在-9月）</h3><p>确定目标院校与专业，收集历年分数线、报录比等数据。</p><h3>二、报名阶段（9-10月）</h3><p>网上报名、确认信息，注意每个省的具体要求。</p><h3>三、初试阶段（12月）</h3><p>全国统一初试，考前一天踩点，带齐证件。</p><h3>四、复试调剂（次年3-4月）</h3><p>初试出分后及时准备复试，未达线可关注调剂信息。</p>"));
        articles.add(article("2025年国家线深度解读：哪些专业在涨？哪些专业在降？",
                "资讯",
                "多专业国家线数据分析，帮你判断报考趋势。",
                "<p>2025年国家线已经公布，整体呈现\"稳中有变\"的态势。</p><p>工学门类略有下降，管理类联考小幅上升，艺术类继续保持高位。</p><p>建议考生结合自身情况理性选择，不要盲目追逐热门。</p>"));
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
}
