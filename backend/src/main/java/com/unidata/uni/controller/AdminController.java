package com.unidata.uni.controller;

import com.unidata.uni.dto.ApiResponse;
import com.unidata.uni.dto.PageResult;
import com.unidata.uni.entity.Activity;
import com.unidata.uni.entity.Article;
import com.unidata.uni.entity.School;
import com.unidata.uni.entity.ScoreLine;
import com.unidata.uni.entity.User;
import com.unidata.uni.service.AdminService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats() {
        return ApiResponse.ok(adminService.stats());
    }

    @GetMapping("/overview")
    public ApiResponse<Map<String, Object>> overview() {
        return ApiResponse.ok(adminService.overview());
    }

    // ---------- 用户 ----------

    @GetMapping("/users")
    public ApiResponse<PageResult<User>> users(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(adminService.users(keyword, page, size));
    }

    @PutMapping("/users/{id}")
    public ApiResponse<User> updateUser(@PathVariable Long id, @RequestBody User patch) {
        return ApiResponse.ok(adminService.updateUser(id, patch));
    }

    @DeleteMapping("/users/{id}")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        adminService.deleteUser(id);
        return ApiResponse.ok();
    }

    @PutMapping("/users/{id}/password")
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        adminService.resetPassword(id, body.get("password"));
        return ApiResponse.ok();
    }

    // ---------- 学校 ----------

    @GetMapping("/schools")
    public ApiResponse<PageResult<School>> schools(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(adminService.schools(keyword, page, size));
    }

    @PostMapping("/schools")
    public ApiResponse<School> createSchool(@RequestBody School school) {
        return ApiResponse.ok(adminService.saveSchool(school));
    }

    @PutMapping("/schools/{id}")
    public ApiResponse<School> updateSchool(@PathVariable Long id, @RequestBody School patch) {
        patch.setId(id);
        return ApiResponse.ok(adminService.saveSchool(patch));
    }

    @DeleteMapping("/schools/{id}")
    public ApiResponse<Void> deleteSchool(@PathVariable Long id) {
        adminService.deleteSchool(id);
        return ApiResponse.ok();
    }

    // ---------- 分数线 ----------

    @GetMapping("/scorelines")
    public ApiResponse<PageResult<ScoreLine>> scoreLines(
            @RequestParam(required = false) Long schoolId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String major,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(adminService.scoreLines(schoolId, year, major, page, size));
    }

    @PostMapping("/scorelines")
    public ApiResponse<ScoreLine> createScoreLine(@RequestBody ScoreLine line) {
        return ApiResponse.ok(adminService.saveScoreLine(line));
    }

    @PutMapping("/scorelines/{id}")
    public ApiResponse<ScoreLine> updateScoreLine(@PathVariable Long id, @RequestBody ScoreLine patch) {
        patch.setId(id);
        return ApiResponse.ok(adminService.saveScoreLine(patch));
    }

    @DeleteMapping("/scorelines/{id}")
    public ApiResponse<Void> deleteScoreLine(@PathVariable Long id) {
        adminService.deleteScoreLine(id);
        return ApiResponse.ok();
    }

    @PostMapping("/scorelines/import")
    public ApiResponse<Map<String, Object>> importScoreLines(@RequestBody Map<String, String> body) {
        return ApiResponse.ok(adminService.importScoreLines(body.get("csv")));
    }

    // ---------- 资讯 ----------

    @GetMapping("/articles")
    public ApiResponse<PageResult<Article>> articles(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(adminService.articles(keyword, page, size));
    }

    @PostMapping("/articles")
    public ApiResponse<Article> createArticle(@RequestBody Article article) {
        return ApiResponse.ok(adminService.saveArticle(article));
    }

    @PutMapping("/articles/{id}")
    public ApiResponse<Article> updateArticle(@PathVariable Long id, @RequestBody Article patch) {
        patch.setId(id);
        return ApiResponse.ok(adminService.saveArticle(patch));
    }

    @DeleteMapping("/articles/{id}")
    public ApiResponse<Void> deleteArticle(@PathVariable Long id) {
        adminService.deleteArticle(id);
        return ApiResponse.ok();
    }

    // ---------- 活动 ----------

    @GetMapping("/activities")
    public ApiResponse<PageResult<Activity>> activities(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(adminService.activities(page, size));
    }

    @PostMapping("/activities")
    public ApiResponse<Activity> createActivity(@RequestBody Activity activity) {
        return ApiResponse.ok(adminService.saveActivity(activity));
    }

    @PutMapping("/activities/{id}")
    public ApiResponse<Activity> updateActivity(@PathVariable Long id, @RequestBody Activity patch) {
        patch.setId(id);
        return ApiResponse.ok(adminService.saveActivity(patch));
    }

    @DeleteMapping("/activities/{id}")
    public ApiResponse<Void> deleteActivity(@PathVariable Long id) {
        adminService.deleteActivity(id);
        return ApiResponse.ok();
    }
}
