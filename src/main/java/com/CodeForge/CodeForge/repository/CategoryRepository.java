package com.codeforge.codeforge.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.codeforge.codeforge.model.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Find a category by its exact name
    Optional<Category> findByName(String name);

    // Find categories by name containing a string (case-insensitive)
    List<Category> findByNameContainingIgnoreCase(String name);

    // Find all active categories
    List<Category> findByIsActiveTrue();

    // Find all inactive categories
    List<Category> findByIsActiveFalse();

    // Custom query to find categories with a problem count greater than a certain value
    @Query("SELECT c FROM Category c WHERE SIZE(c.problems) > :count")
    List<Category> findByProblemCountGreaterThan(@Param("count") int count);

    // Check if a category exists by name (useful for validation before creation)
    boolean existsByName(String name);

    // Check if a category exists by name and ID is not equal (for update validation)
    boolean existsByNameAndIdNot(String name, Long id);
}