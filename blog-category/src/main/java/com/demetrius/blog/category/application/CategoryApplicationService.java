package com.demetrius.blog.category.application;

import com.demetrius.blog.category.domain.category.entity.Category;
import com.demetrius.blog.category.domain.category.repository.CategoryRepository;
import com.demetrius.blog.category.interfaces.dto.*;
import com.demetrius.blog.common.exception.BizException;
import com.demetrius.blog.common.exception.ErrorCode;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CategoryApplicationService {

    private final CategoryRepository categoryRepository;

    public CategoryApplicationService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public List<CategoryVO> getCategoryTree() {
        List<Category> all = categoryRepository.findAll();
        return buildTree(all.stream().map(this::toVO).collect(Collectors.toList()));
    }

    public CategoryVO getById(Long id) {
        Category category = categoryRepository.findById(id);
        if (category == null) {
            throw ErrorCode.CATEGORY_NOT_FOUND.toException();
        }
        return toVO(category);
    }

    public Long create(CreateCategoryRequest request) {
        if (request.getParentId() != null && request.getParentId() > 0) {
            Category parent = categoryRepository.findById(request.getParentId());
            if (parent == null) {
                throw ErrorCode.CATEGORY_NOT_FOUND.toException();
            }
        }

        Category category = Category.builder()
                .name(request.getName())
                .parentId(request.getParentId() != null ? request.getParentId() : 0L)
                .slug(request.getSlug())
                .description(request.getDescription())
                .icon(request.getIcon())
                .sortOrder(request.getSortOrder() != null ? request.getSortOrder() : 0)
                .articleCount(0)
                .build();
        category.initCreateTime();
        categoryRepository.save(category);
        return category.getId();
    }

    public void update(Long id, UpdateCategoryRequest request) {
        Category category = categoryRepository.findById(id);
        if (category == null) {
            throw ErrorCode.CATEGORY_NOT_FOUND.toException();
        }
        category.setName(request.getName());
        category.setSlug(request.getSlug());
        category.setDescription(request.getDescription());
        category.setIcon(request.getIcon());
        if (request.getSortOrder() != null) {
            category.setSortOrder(request.getSortOrder());
        }
        category.updateTime();
        categoryRepository.save(category);
    }

    public void delete(Long id) {
        Category category = categoryRepository.findById(id);
        if (category == null) {
            throw ErrorCode.CATEGORY_NOT_FOUND.toException();
        }

        boolean hasChildren = categoryRepository.existsByParentId(id);
        if (hasChildren) {
            throw new BizException(400, "该分类下存在子分类，无法删除");
        }

        if (category.getArticleCount() != null && category.getArticleCount() > 0) {
            throw ErrorCode.CATEGORY_HAS_ARTICLE.toException();
        }

        categoryRepository.delete(id);
    }

    private List<CategoryVO> buildTree(List<CategoryVO> all) {
        Map<Long, List<CategoryVO>> groupByParent = all.stream()
                .filter(c -> c.getParentId() != null && c.getParentId() > 0)
                .collect(Collectors.groupingBy(CategoryVO::getParentId));

        List<CategoryVO> roots = all.stream()
                .filter(c -> c.getParentId() == null || c.getParentId() == 0)
                .sorted((a, b) -> {
                    int sa = a.getSortOrder() != null ? a.getSortOrder() : 0;
                    int sb = b.getSortOrder() != null ? b.getSortOrder() : 0;
                    return sa - sb;
                })
                .toList();

        for (CategoryVO root : roots) {
            root.setChildren(buildChildren(root.getId(), groupByParent));
        }
        return new ArrayList<>(roots);
    }

    private List<CategoryVO> buildChildren(Long parentId, Map<Long, List<CategoryVO>> map) {
        List<CategoryVO> children = map.getOrDefault(parentId, new ArrayList<>());
        children.sort((a, b) -> {
            int sa = a.getSortOrder() != null ? a.getSortOrder() : 0;
            int sb = b.getSortOrder() != null ? b.getSortOrder() : 0;
            return sa - sb;
        });
        for (CategoryVO child : children) {
            child.setChildren(buildChildren(child.getId(), map));
        }
        return children;
    }

    private CategoryVO toVO(Category c) {
        CategoryVO vo = new CategoryVO();
        vo.setId(c.getId());
        vo.setName(c.getName());
        vo.setSlug(c.getSlug());
        vo.setDescription(c.getDescription());
        vo.setIcon(c.getIcon());
        vo.setSortOrder(c.getSortOrder());
        vo.setArticleCount(c.getArticleCount());
        vo.setParentId(c.getParentId() != null && c.getParentId() > 0 ? c.getParentId() : null);
        vo.setCreateTime(c.getCreateTime());
        return vo;
    }
}
