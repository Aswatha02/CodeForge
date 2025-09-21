package com.CodeForge.CodeForge.Controllers;

import com.CodeForge.CodeForge.model.Category;
import com.CodeForge.CodeForge.services.CategoryService;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @PostMapping
    public Category createCategory(@RequestBody Category category) {
        return categoryService.createCategory(category);
    }

    @GetMapping
    public List<Category> listCategories() {
        return categoryService.getAllCategories();
    }
}
