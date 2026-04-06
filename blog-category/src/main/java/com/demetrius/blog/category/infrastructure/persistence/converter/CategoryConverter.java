package com.demetrius.blog.category.infrastructure.persistence.converter;

import com.demetrius.blog.category.domain.category.entity.Category;
import com.demetrius.blog.category.domain.category.valueobject.CategoryStatus;
import com.demetrius.blog.category.infrastructure.persistence.po.CategoryPO;
import org.springframework.stereotype.Component;

@Component
public class CategoryConverter {

    public Category toDomain(CategoryPO po) {
        if (po == null) return null;
        return Category.builder()
                .id(po.getId())
                .name(po.getName())
                .setSlug(po.getSlug())
                .setDescription(po.getDescription())
                .setIcon(po.getIcon())
                .setParentId(po.getParentId())
                .setSortOrder(po.getSortOrder())
                .setArticleCount(po.getArticleCount())
                .setStatus(CategoryStatus.of(po.getStatus()))
                .setCreateTime(po.getCreateTime())
                .setUpdateTime(po.getUpdateTime())
                .build();
    }

    public CategoryPO toPO(Category domain) {
        if (domain == null) return null;
        CategoryPO po = new CategoryPO();
        po.setId(domain.getId());
        po.setName(domain.getName());
        po.setSlug(domain.getSlug());
        po.setDescription(domain.getDescription());
        po.setIcon(domain.getIcon());
        po.setParentId(domain.getParentId());
        po.setSortOrder(domain.getSortOrder());
        po.setArticleCount(domain.getArticleCount());
        po.setStatus(domain.getStatus().getCode());
        po.setCreateTime(domain.getCreateTime());
        po.setUpdateTime(domain.getUpdateTime());
        return po;
    }
}
