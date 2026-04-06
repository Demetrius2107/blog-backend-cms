package com.demetrius.blog.comment.interfaces.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CommentVO {

    private Long id;
    private Long articleId;
    private Long userId;
    private String username;
    private String userAvatar;
    private Long parentId;
    private Long replyToId;
    private Long replyToUserId;
    private String replyToUsername;
    private String content;
    private Integer status;
    private Integer likeCount;
    private List<CommentVO> children;
    private LocalDateTime createTime;
}
