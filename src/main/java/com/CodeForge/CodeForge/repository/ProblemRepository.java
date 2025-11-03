package com.CodeForge.CodeForge.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.CodeForge.CodeForge.model.Problem;
import com.CodeForge.CodeForge.model.Problem.Difficulty;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {

    // Use EntityGraph to fetch all related entities in a single query
    @EntityGraph(attributePaths = {"categories", "codeTemplates", "testCases", "creator"})
    @Query("SELECT p FROM Problem p WHERE p.id = :id")
    Optional<Problem> findByIdWithDetails(@Param("id") Long id);

    @EntityGraph(attributePaths = {"categories", "codeTemplates", "testCases", "creator"})
    @Query("SELECT p FROM Problem p WHERE p.slug = :slug")
    Optional<Problem> findBySlugWithDetails(@Param("slug") String slug);

    @EntityGraph(attributePaths = {"categories", "creator"})
    @Query("SELECT p FROM Problem p WHERE p.status = com.CodeForge.CodeForge.model.Problem$Status.ACTIVE")
    List<Problem> findAllActiveWithDetails();

    // Keep original methods for backward compatibility
    Optional<Problem> findBySlug(String slug);

    @Query("SELECT p FROM Problem p WHERE p.status = com.CodeForge.CodeForge.model.Problem$Status.ACTIVE")
    List<Problem> findAllActive();

    @Query("SELECT p FROM Problem p JOIN p.categories c WHERE c.id = :categoryId AND p.status = com.CodeForge.CodeForge.model.Problem$Status.ACTIVE")
    List<Problem> findByCategoryId(@Param("categoryId") Long categoryId);

    List<Problem> findByDifficulty(Problem.Difficulty difficulty);

    @Query("SELECT p FROM Problem p WHERE p.creator.id = :creatorId")
    List<Problem> findByCreatorId(@Param("creatorId") Long creatorId);

    @Query("SELECT COUNT(p) FROM Problem p WHERE p.difficulty = :difficulty AND p.status = com.CodeForge.CodeForge.model.Problem$Status.ACTIVE")
    long countByDifficulty(@Param("difficulty") Problem.Difficulty difficulty);

    @Query("SELECT p FROM Problem p WHERE p.status = com.CodeForge.CodeForge.model.Problem$Status.ACTIVE ORDER BY p.createdAt DESC")
    List<Problem> findRecentProblems();

      // Find problems by category name (supports multiple categories)
    @Query("SELECT DISTINCT p FROM Problem p JOIN p.categories c WHERE c.name LIKE %:categoryName%")
    List<Problem> findByCategoriesNameContainingIgnoreCase(@Param("categoryName") String categoryName);
    
    // Find problems by specific category name
    @Query("SELECT p FROM Problem p JOIN p.categories c WHERE c.name = :categoryName")
    List<Problem> findByCategoriesName(@Param("categoryName") String categoryName);
    
    // Find problems by difficulty and category
    @Query("SELECT DISTINCT p FROM Problem p JOIN p.categories c WHERE p.difficulty = :difficulty AND c.name = :categoryName")
    List<Problem> findByDifficultyAndCategoriesName(@Param("difficulty") Difficulty difficulty, 
                                                   @Param("categoryName") String categoryName);
}