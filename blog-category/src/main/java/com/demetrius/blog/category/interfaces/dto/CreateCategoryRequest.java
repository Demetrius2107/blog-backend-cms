package com.demetrius.blog.category.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateCategoryRequest {

    @NotBlank(message = "分类名称不能为空")
    private String name;

    private Long parentId;
    private String slug;
    private String description;
    private String icon;
    private Integer sortOrder;
}
