package com.demetrius.blog.category.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UpdateCategoryRequest {

    @NotBlank(message = "分类名称不能为空")
    private String name;

    private String slug;
    private String description;
    private String icon;
    private Integer sortOrder;
}
