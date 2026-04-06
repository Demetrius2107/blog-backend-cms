package com.demetrius.blog.article.infrastructure.persistence.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("t_article")
public class ArticlePO {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String title;
    private String summary;
    private String content;
    private String contentHtml;
    private String coverImage;
    private Long categoryId;
    private Integer status;
    private Integer isTop;
    private Integer isOriginal;
    private String sourceUrl;
    private String tags;
    private Long authorId;
    private Long viewCount;
    private Long likeCount;
    private Integer commentCount;
    private Integer wordCount;
    private LocalDateTime publishTime;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
