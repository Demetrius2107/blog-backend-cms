package com.demetrius.blog.comment.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReplyCommentRequest {

    @NotNull(message = "文章ID不能为空")
    private Long articleId;

    @NotNull(message = "父评论ID不能为空")
    private Long parentId;

    @NotNull(message = "被回复评论ID不能为空")
    private Long replyToId;

    @NotBlank(message = "回复内容不能为空")
    private String content;
}
