package com.demetrius.vellastra.article.interfaces.facade;

import com.demetrius.vellastra.article.application.ArticleApplicationService;
import com.demetrius.vellastra.article.application.DashboardApplicationService;
import com.demetrius.vellastra.article.interfaces.dto.*;
import com.demetrius.vellastra.common.response.PageResult;
import com.demetrius.vellastra.common.response.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * <p>Title: ArticleController</p>
 * <p>Description: 文章 RESTful 接口控制器</p>
 * <p>我们在命运的两端 是否有相似的痛感</p>
 * <p>在每个忏悔的夜晚</p>
 * <p>可我无法再隐藏 想要倾诉的愿望</p>
 * <p>只有冷漠以对 她才不会再受伤</p>
 * <p>项目名称: Vellastra</p>
 *
 * @author wanqiu
 * @since 1.1
 * @createTime 2026-05-17
 * @updateTime 2026-07-05
 *
 * <p>Copyright © 2026 wanqiu All rights reserved</p>
 
 */
@RestController
@RequestMapping("/article")
public class ArticleController {

    private final ArticleApplicationService articleApplicationService;
    private final DashboardApplicationService dashboardApplicationService;

    public ArticleController(ArticleApplicationService articleApplicationService,
                             DashboardApplicationService dashboardApplicationService) {
        this.articleApplicationService = articleApplicationService;
        this.dashboardApplicationService = dashboardApplicationService;
    }

    // ======================== CRUD基础操作 ========================

    /**
     * 创建文章
     *
     * @param request 创建文章请求
     * @param userId  用户ID（请求头）
     * @return 文章ID
     */
    @PostMapping
    public Result<Long> createArticle(@Valid @RequestBody CreateArticleRequest request,
                                      @RequestHeader("X-User-Id") Long userId) {
        return Result.success(articleApplicationService.createArticle(request, userId));
    }

    /**
     * 更新文章
     *
     * @param id         文章ID
     * @param request    更新文章请求
     * @param userId     当前用户ID（请求头）
     * @param roles      当前用户角色（请求头，逗号分隔）
     */
    @PutMapping("/{id}")
    public Result<Void> updateArticle(@PathVariable Long id,
                                      @Valid @RequestBody UpdateArticleRequest request,
                                      @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                      @RequestHeader(value = "X-Roles", required = false) String roles) {
        articleApplicationService.updateArticle(id, request, userId, roles);
        return Result.success();
    }

    /**
     * 删除文章（已发布的文章不可删除）
     *
     * @param id     文章ID
     * @param userId 当前用户ID（请求头）
     * @param roles  当前用户角色（请求头，逗号分隔）
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id,
                                      @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                      @RequestHeader(value = "X-Roles", required = false) String roles) {
        articleApplicationService.deleteArticle(id, userId, roles);
        return Result.success();
    }

    /**
     * 查看文章详情
     *
     * @param id 文章ID
     * @return 文章视图对象
     */
    @GetMapping("/{id}")
    public Result<ArticleVO> getArticle(@PathVariable Long id) {
        return Result.success(articleApplicationService.getArticleById(id));
    }

    /**
     * 分页查询文章列表
     *
     * @param current   页码（默认1）
     * @param size      每页条数（默认10）
     * @param categoryId 分类ID（可选）
     * @param keyword   关键词搜索（可选，匹配标题）
     * @param authorId  作者ID筛选（可选）
     * @return 分页文章列表
     */
    @GetMapping
    public Result<PageResult<ArticleVO>> listArticles(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long authorId) {
        return Result.success(articleApplicationService.listArticles(
                current, size, categoryId, keyword, authorId));
    }

    // ======================== 文章状态管理 ========================

    /**
     * 发布文章（草稿→已发布）
     */
    @PatchMapping("/{id}/publish")
    public Result<Void> publishArticle(@PathVariable Long id) {
        articleApplicationService.publish(id);
        return Result.success();
    }

    /**
     * 撤回发布（已发布→下架）
     */
    @PatchMapping("/{id}/withdraw")
    public Result<Void> withdrawArticle(@PathVariable Long id) {
        articleApplicationService.withdraw(id);
        return Result.success();
    }

    // ======================== 置顶/取消置顶 ========================

    /**
     * 设置/取消置顶
     */
    @PatchMapping("/{id}/top")
    public Result<Void> topArticle(@PathVariable Long id, @RequestParam boolean top) {
        articleApplicationService.topArticle(id, top);
        return Result.success();
    }

    // ======================== 互动统计 ========================

    /**
     * 浏览计数（IP+时间窗口防刷：同一 IP 对同一文章 10 分钟内只计一次）
     */
    @PostMapping("/{id}/view")
    public Result<Void> viewArticle(@PathVariable Long id, HttpServletRequest request) {
        articleApplicationService.incrementViewCount(id, resolveClientIp(request));
        return Result.success();
    }

    /**
     * 解析客户端真实 IP（兼容反向代理透传 X-Forwarded-For）
     */
    private String resolveClientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    /**
     * 点赞/取消点赞（toggle 模式）
     *
     * @return true=操作后已点赞，false=操作后已取消
     */
    @PostMapping("/{id}/like")
    public Result<Boolean> likeArticle(@PathVariable Long id,
                                       @RequestHeader("X-User-Id") Long userId) {
        return Result.success(articleApplicationService.toggleLike(id, userId));
    }

    // ======================== 数据仪表盘 ========================

    /**
     * 获取仪表盘数据（总览统计 + 热门文章 + 趋势等）
     *
     * @return 仪表盘视图对象
     */
    @GetMapping("/dashboard")
    public Result<DashboardVO> getDashboard() {
        return Result.success(dashboardApplicationService.getDashboard());
    }

    // ======================== 最新文章 ========================

    /**
     * 获取最新文章
     *
     * @param size 获取数量（默认5）
     * @return 最新文章列表
     */
    @GetMapping("/latest")
    public Result<List<ArticleVO>> getLatestArticle(@RequestParam(defaultValue = "5") int size) {
        return Result.success(articleApplicationService.getLatestArticles(size));
    }

    // ======================== 批量操作 ========================

    /**
     * 批量操作文章（删除 / 发布）
     *
     * @param request 批量操作请求（ids + action）
     */
    @PostMapping("/batch")
    public Result<Void> batchOperation(@Valid @RequestBody BatchArticleRequest request) {
        articleApplicationService.batchOperation(request);
        return Result.success();
    }

    // ======================== SSG 导出（供静态站构建拉取） ========================

    /**
     * 导出全部已发布文章，供静态站点生成器（VitePress 等）构建时拉取。
     * 路径在网关白名单内，公开访问（只含已发布内容，无敏感数据）。
     *
     * @return 已发布文章列表
     */
    @GetMapping("/export/articles")
    public Result<List<ArticleVO>> exportArticles() {
        return Result.success(articleApplicationService.exportPublishedArticles());
    }

    /**
     * 导出站点配置（名称/描述/URL），供静态站点生成器构建时拉取。
     *
     * @return 站点配置键值
     */
    @GetMapping("/export/site-config")
    public Result<Map<String, String>> exportSiteConfig() {
        return Result.success(articleApplicationService.exportSiteConfig());
    }

}
