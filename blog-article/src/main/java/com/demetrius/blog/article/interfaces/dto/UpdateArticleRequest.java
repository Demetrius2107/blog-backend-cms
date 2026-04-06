package com.demetrius.blog.article.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateArticleRequest {

    @NotBlank(message = "标题不能为空")
    private String title;

    private String content;
    private String summary;
    private String coverImage;
    private Long categoryId;
    private Integer status;
    private String tags;
}
