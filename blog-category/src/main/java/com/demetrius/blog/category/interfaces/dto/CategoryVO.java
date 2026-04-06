package com.demetrius.blog.category.interfaces.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class CategoryVO {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private String icon;
    private Integer sortOrder;
    private Integer articleCount;
    private Long parentId;
    private List<CategoryVO> children;
    private LocalDateTime createTime;
}
