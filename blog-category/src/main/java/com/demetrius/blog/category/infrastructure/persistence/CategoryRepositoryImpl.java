package com.demetrius.blog.category.infrastructure.persistence;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.demetrius.blog.category.domain.category.entity.Category;
import com.demetrius.blog.category.domain.category.repository.CategoryRepository;
import com.demetrius.blog.category.infrastructure.persistence.converter.CategoryConverter;
import com.demetrius.blog.category.infrastructure.persistence.mapper.CategoryMapper;
import com.demetrius.blog.category.infrastructure.persistence.po.CategoryPO;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class CategoryRepositoryImpl implements CategoryRepository {

    private final CategoryMapper categoryMapper;
    private final CategoryConverter categoryConverter;

    public CategoryRepositoryImpl(CategoryMapper categoryMapper, CategoryConverter categoryConverter) {
        this.categoryMapper = categoryMapper;
        this.categoryConverter = categoryConverter;
    }

    @Override
    public Category findById(Long id) {
        CategoryPO po = categoryMapper.selectById(id);
        return po != null ? categoryConverter.toDomain(po) : null;
    }

    @Override
    public List<Category> findAll() {
        return categoryMapper.selectList(
                new LambdaQueryWrapper<CategoryPO>().orderByAsc(CategoryPO::getSortOrder)
        ).stream().map(categoryConverter::toDomain).toList();
    }

    @Override
    public boolean existsByParentId(Long parentId) {
        return categoryMapper.exists(
                new LambdaQueryWrapper<CategoryPO>().eq(CategoryPO::getParentId, parentId)
        );
    }

    @Override
    public void save(Category category) {
        CategoryPO po = categoryConverter.toPO(category);
        if (po.getId() == null) {
            categoryMapper.insert(po);
            category.setId(po.getId());
        } else {
            categoryMapper.updateById(po);
        }
    }

    @Override
    public void delete(Long id) {
        categoryMapper.deleteById(id);
    }
}
