package com.demetrius.blog.article.interfaces.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ArticleVO {

    private Long id;
    private String title;
    private String content;
    private String summary;
    private String coverImage;
    private Long categoryId;
    private String categoryName;
    private Integer status;
    private String tags;
    private Long authorId;
    private String authorName;
    private Long viewCount;
    private Long likeCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
