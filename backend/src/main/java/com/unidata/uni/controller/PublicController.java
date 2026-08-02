package com.unidata.uni.controller;

import com.unidata.uni.dto.ApiResponse;
import com.unidata.uni.dto.PageResult;
import com.unidata.uni.dto.ScoreLineView;
import com.unidata.uni.entity.Activity;
import com.unidata.uni.entity.Article;
import com.unidata.uni.entity.NationalLine;
import com.unidata.uni.entity.School;
import com.unidata.uni.service.PublicService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final PublicService publicService;

    public PublicController(PublicService publicService) {
        this.publicService = publicService;
    }

    @GetMapping("/home")
    public ApiResponse<Map<String, Object>> home() {
        return ApiResponse.ok(publicService.home());
    }

    @GetMapping("/schools")
    public ApiResponse<PageResult<School>> schools(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String province,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String level,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int size) {
        return ApiResponse.ok(publicService.schools(name, province, category, level, page, size));
    }

    @GetMapping("/schools/{id}")
    public ApiResponse<Map<String, Object>> schoolDetail(@PathVariable Long id) {
        return ApiResponse.ok(publicService.schoolDetail(id));
    }

    @GetMapping("/scorelines")
    public ApiResponse<PageResult<ScoreLineView>> scoreLines(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String major,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        // 会员自动解锁专享数据，游客仅返回公开数据
        return ApiResponse.ok(publicService.scoreLines(schoolId, year, major, page, size));
    }

    @GetMapping("/national-lines")
    public ApiResponse<List<NationalLine>> nationalLines(
            @RequestParam(required = false) Integer year) {
        return ApiResponse.ok(publicService.nationalLines(year));
    }

    @GetMapping("/filters")
    public ApiResponse<Map<String, Object>> filters() {
        return ApiResponse.ok(publicService.filters());
    }

    /** 代理加载研招网官方分数线图片，避免跨域/防盗链限制 */
    @GetMapping(value = "/score-image", produces = MediaType.IMAGE_PNG_VALUE)
    public void scoreImage(@RequestParam String url, HttpServletResponse response) throws IOException {
        if (url == null || !url.matches("https://t[1-4]\\.chei\\.com\\.cn/news/img/.*")) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .followRedirects(HttpClient.Redirect.NORMAL)
                .build();
        HttpRequest req = HttpRequest.newBuilder(URI.create(url))
                .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 Chrome/120 Safari/537.36")
                .header("Referer", "https://yz.chsi.com.cn/")
                .timeout(Duration.ofSeconds(20))
                .GET()
                .build();
        try {
            HttpResponse<InputStream> resp = client.send(req, HttpResponse.BodyHandlers.ofInputStream());
            if (resp.statusCode() != 200) {
                response.sendError(HttpServletResponse.SC_BAD_GATEWAY);
                return;
            }
            String type = resp.headers().firstValue("Content-Type").orElse("image/png");
            response.setContentType(type);
            response.setHeader("Cache-Control", "public, max-age=86400");
            try (InputStream in = resp.body(); OutputStream out = response.getOutputStream()) {
                in.transferTo(out);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            response.sendError(HttpServletResponse.SC_BAD_GATEWAY);
        } catch (IOException e) {
            response.sendError(HttpServletResponse.SC_BAD_GATEWAY);
        }
    }

    @GetMapping("/articles")
    public ApiResponse<PageResult<Article>> articles(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(publicService.articles(category, keyword, page, size));
    }

    @GetMapping("/articles/{id}")
    public ApiResponse<Article> articleDetail(@PathVariable Long id) {
        return ApiResponse.ok(publicService.articleDetail(id));
    }

    @GetMapping("/activities")
    public ApiResponse<PageResult<Activity>> activities(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(publicService.activities(page, size));
    }
}
