package com.CodeForge.CodeForge.services;

import com.CodeForge.CodeForge.model.Category;
import com.CodeForge.CodeForge.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Autowired
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public Category createCategory(Category category) {
        if (categoryRepository.findByName(category.getName()).isPresent()) {
            throw new RuntimeException("Category name already exists");
        }
        return categoryRepository.save(category);
    }

    public Category createCategory(String name, String description) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        return createCategory(category);
    }

    public List<Category> getAllCategories() {
        return categoryRepository.findAllWithProblems();
    }

    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findByIdWithProblems(id);
    }

    public Optional<Category> getCategoryByName(String name) {
        return categoryRepository.findByName(name);
    }

    public void deleteCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        categoryRepository.delete(category);
    }

    // Add this method for creating categories from admin dashboard with color
    public Category createCategory(String name, String description, String color) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        category.setColor(color);
        return createCategory(category);
    }

    // Add this method for updating categories
    public Category updateCategory(Long categoryId, String name, String description) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        category.setName(name);
        category.setDescription(description);

        return categoryRepository.save(category);
    }

    // Add this method for updating categories with color
    public Category updateCategory(Long categoryId, String name, String description, String color) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        category.setName(name);
        category.setDescription(description);
        category.setColor(color);

        return categoryRepository.save(category);
    }

    // Add this method for admin dashboard
    public List<Category> getAllCategoriesSimple() {
        return categoryRepository.findAll();
    }
}
