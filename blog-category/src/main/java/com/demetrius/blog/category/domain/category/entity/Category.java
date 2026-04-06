package com.demetrius.blog.category.domain.category.entity;

import com.demetrius.blog.category.domain.category.valueobject.CategoryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private String icon;
    private Long parentId;
    private Integer sortOrder;
    private Integer articleCount;
    private CategoryStatus status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public void initCreateTime() {
        this.createTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
        this.status = CategoryStatus.ENABLED;
    }

    public void updateTime() {
        this.updateTime = LocalDateTime.now();
    }
}
