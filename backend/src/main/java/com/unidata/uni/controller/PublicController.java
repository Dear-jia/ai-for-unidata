package com.unidata.uni.controller;

import com.unidata.uni.dto.ApiResponse;
import com.unidata.uni.dto.PageResult;
import com.unidata.uni.dto.ScoreLineView;
import com.unidata.uni.entity.Activity;
import com.unidata.uni.entity.Article;
import com.unidata.uni.entity.School;
import com.unidata.uni.service.PublicService;
import org.springframework.web.bind.annotation.*;

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
