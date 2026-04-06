package com.demetrius.blog.category.domain.category.repository;

import com.demetrius.blog.category.domain.category.entity.Category;

import java.util.List;

public interface CategoryRepository {

    Category findById(Long id);

    List<Category> findAll();

    boolean existsByParentId(Long parentId);

    void save(Category category);

    void delete(Long id);
}
