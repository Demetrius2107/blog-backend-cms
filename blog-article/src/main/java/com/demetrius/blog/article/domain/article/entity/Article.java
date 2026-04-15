package com.demetrius.blog.article.domain.article.entity;

import com.demetrius.blog.article.domain.article.valueobject.ArticleStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Article {

    /* ID*/
    private Long id;

    /* 文章标题*/
    private String title;

    /* 内容*/
    private String content;

    /* 总结*/
    private String summary;

    /* 图片*/
    private String coverImage;


    private Long categoryId;


    private Integer status;


    private String tags;


    private Long authorId;


    private Long viewCount;


    private Long likeCount;


    private Integer commentCount;


    private Integer isTop;

    /* 发布时间*/
    private LocalDateTime publishTime;

    /* 创建时间*/
    private LocalDateTime createTime;

    /* 更新时间*/
    private LocalDateTime updateTime;

    public boolean isPublished() {
        return this.status == ArticleStatus.PUBLISHED.getCode();
    }

    public void publish() {
        this.status = ArticleStatus.PUBLISHED.getCode();
        this.updateTime();
    }

    public void draft() {
        this.status = ArticleStatus.DRAFT.getCode();
        this.updateTime();
    }

    public void initCreateTime() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }

    public void updateTime() {
        this.updateTime = LocalDateTime.now();
    }
}
