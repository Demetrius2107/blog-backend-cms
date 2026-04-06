package com.demetrius.blog.category.interfaces.facade;

import com.demetrius.blog.category.application.CategoryApplicationService;
import com.demetrius.blog.category.interfaces.dto.CategoryVO;
import com.demetrius.blog.category.interfaces.dto.CreateCategoryRequest;
import com.demetrius.blog.category.interfaces.dto.UpdateCategoryRequest;
import com.demetrius.blog.common.response.Result;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {

    private final CategoryApplicationService categoryApplicationService;

    public CategoryController(CategoryApplicationService categoryApplicationService) {
        this.categoryApplicationService = categoryApplicationService;
    }

    @GetMapping("/tree")
    public Result<List<CategoryVO>> tree() {
        return Result.success(categoryApplicationService.getCategoryTree());
    }

    @GetMapping("/{id}")
    public Result<CategoryVO> getCategory(@PathVariable Long id) {
        return Result.success(categoryApplicationService.getById(id));
    }

    @PostMapping
    public Result<Long> create(@Valid @RequestBody CreateCategoryRequest request) {
        return Result.success(categoryApplicationService.create(request));
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody UpdateCategoryRequest request) {
        categoryApplicationService.update(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        categoryApplicationService.delete(id);
        return Result.success();
    }
}
