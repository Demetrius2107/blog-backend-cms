package com.demetrius.blog.comment.interfaces.facade;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.demetrius.blog.comment.application.CommentApplicationService;
import com.demetrius.blog.comment.interfaces.dto.*;
import com.demetrius.blog.common.response.PageResult;
import com.demetrius.blog.common.response.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/comment")
public class CommentController {

    private final CommentApplicationService commentApplicationService;

    public CommentController(CommentApplicationService commentApplicationService) {
        this.commentApplicationService = commentApplicationService;
    }

    @GetMapping
    public Result<PageResult<CommentVO>> list(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) Long articleId,
            @RequestParam(required = false) Integer status) {
        return Result.success(commentApplicationService.list(current, size, articleId, status));
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody CreateCommentRequest request,
                               @RequestHeader("X-User-Id") Long userId) {
        return Result.success(commentApplicationService.create(request, userId));
    }

    @PostMapping("/reply")
    public Result<Long> reply(@Valid @RequestBody ReplyCommentRequest request,
                              @RequestHeader("X-User-Id") Long userId) {
        return Result.success(commentApplicationService.reply(request, userId));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        commentApplicationService.delete(id);
        return Result.success();
    }

    @PatchMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable Long id, @RequestParam Integer status) {
        commentApplicationService.audit(id, status);
        return Result.success();
    }
}
