package com.demetrius.blog.article.interfaces.facade;

import com.demetrius.blog.article.application.ArticleApplicationService;
import com.demetrius.blog.article.interfaces.dto.*;
import com.demetrius.blog.common.response.PageResult;
import com.demetrius.blog.common.response.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/article")
public class ArticleController {

    private final ArticleApplicationService articleApplicationService;

    public ArticleController(ArticleApplicationService articleApplicationService) {
        this.articleApplicationService = articleApplicationService;
    }

    @PostMapping
    public Result<Long> createArticle(@Valid @RequestBody CreateArticleRequest request,
                                      @RequestHeader("X-User-Id") Long userId) {
        return Result.success(articleApplicationService.createArticle(request, userId));
    }

    @PutMapping("/{id}")
    public Result<Void> updateArticle(@PathVariable Long id,
                                      @Valid @RequestBody UpdateArticleRequest request) {
        articleApplicationService.updateArticle(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteArticle(@PathVariable Long id) {
        articleApplicationService.deleteArticle(id);
        return Result.success();
    }

    @GetMapping("/{id}")
    public Result<ArticleVO> getArticle(@PathVariable Long id) {
        return Result.success(articleApplicationService.getArticleById(id));
    }

    @GetMapping
    public Result<PageResult<ArticleVO>> listArticles(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long categoryId) {
        return Result.success(articleApplicationService.listArticles(current, size, categoryId));
    }
}
