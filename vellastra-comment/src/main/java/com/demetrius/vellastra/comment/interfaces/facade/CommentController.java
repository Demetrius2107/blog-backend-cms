package com.demetrius.vellastra.comment.interfaces.facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demetrius.vellastra.comment.application.CommentApplicationService;
import com.demetrius.vellastra.comment.interfaces.dto.*;
import com.demetrius.vellastra.common.response.PageResult;
import com.demetrius.vellastra.common.response.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

/**
 * <p>Title: CommentController</p>
 * <p>Description: 评论控制器，处理评论的增删改查和审核</p>
 * <p>项目名称: Vellastra</p>
 *
 * @author wanqiu
 * @since 1.1
 * @createTime 2026-05-17
 * @updateTime 2026-07-05
 *
 * Copyright © 2026 wanqiu All rights reserved
 
 */
@RestController
@RequestMapping("/comment")
public class CommentController {

    private final CommentApplicationService commentApplicationService;

    public CommentController(CommentApplicationService commentApplicationService) {
        this.commentApplicationService = commentApplicationService;
    }

    /**
     * 分页查询评论列表
     *
     * @param current   页码
     * @param size      每页条数
     * @param articleId 文章ID（可选）
     * @param status    审核状态（可选）
     * @return 分页评论列表
     */
    @GetMapping
    public Result<PageResult<CommentVO>> list(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long articleId,
            @RequestParam(required = false) Integer status) {
        return Result.success(commentApplicationService.list(current, size, articleId, status));
    }

    /**
     * 创建评论
     *
     * @param request 创建评论请求
     * @param userId  当前登录用户ID
     * @return 评论ID
     */
    @PostMapping
    public Result<Long> create(@Valid @RequestBody CreateCommentRequest request,
                               @RequestHeader("X-User-Id") Long userId) {
        return Result.success(commentApplicationService.create(request, userId));
    }

    /**
     * 回复评论
     *
     * @param request 回复评论请求
     * @param userId  当前登录用户ID
     * @return 评论ID
     */
    @PostMapping("/reply")
    public Result<Long> reply(@Valid @RequestBody ReplyCommentRequest request,
                              @RequestHeader("X-User-Id") Long userId) {
        return Result.success(commentApplicationService.reply(request, userId));
    }

    /**
     * 删除评论（仅评论作者或管理员）
     *
     * @param id     评论ID
     * @param userId 当前用户ID（请求头）
     * @param roles  当前用户角色（请求头，逗号分隔）
     */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id,
                               @RequestHeader(value = "X-User-Id", required = false) Long userId,
                               @RequestHeader(value = "X-Roles", required = false) String roles) {
        commentApplicationService.delete(id, userId, roles);
        return Result.success();
    }

    /**
     * 审核评论（仅管理员）
     *
     * @param id     评论ID
     * @param status 目标状态（1-通过 2-拒绝）
     * @param roles  当前用户角色（请求头，逗号分隔）
     */
    @PatchMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable Long id, @RequestParam Integer status,
                              @RequestHeader(value = "X-Roles", required = false) String roles) {
        commentApplicationService.audit(id, status, roles);
        return Result.success();
    }
}
