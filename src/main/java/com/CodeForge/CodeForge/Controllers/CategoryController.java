package com.CodeForge.CodeForge.Controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.CodeForge.CodeForge.model.Category;
import com.CodeForge.CodeForge.model.Problem;
import com.CodeForge.CodeForge.services.CategoryService;
import com.CodeForge.CodeForge.services.ProblemService;

@RestController
@RequestMapping("/api/categories")

public class CategoryController {
    
    private final CategoryService categoryService;
    private final ProblemService problemService;

    // Constructor injection instead of @RequiredArgsConstructor
    @Autowired
    public CategoryController(CategoryService categoryService, ProblemService problemService) {
        this.categoryService = categoryService;
        this.problemService = problemService;
    }

    @PostMapping
    public ResponseEntity<?> createCategory(@RequestBody Category category) {
        try {
            Category createdCategory = categoryService.createCategory(category);
            return ResponseEntity.ok(createdCategory);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoryById(@PathVariable Long id) {
        try {
            Category category = categoryService.getCategoryById(id)
                    .orElseThrow(() -> new RuntimeException("Category not found"));
            return ResponseEntity.ok(category);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/{categoryId}/problems")
    public ResponseEntity<?> getProblemsByCategory(@PathVariable Long categoryId) {
        try {
            List<Problem> problems = problemService.getProblemsByCategory(categoryId);
            return ResponseEntity.ok(problems);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // Admin endpoints
    @DeleteMapping("/admin/{categoryId}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long categoryId) {
        try {
            categoryService.deleteCategory(categoryId);
            return ResponseEntity.ok().body(Map.of("message", "Category deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}