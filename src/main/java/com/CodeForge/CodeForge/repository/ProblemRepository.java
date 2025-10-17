package com.CodeForge.CodeForge.repository;

import com.CodeForge.CodeForge.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {
    
    Optional<Problem> findBySlug(String slug);
    List<Problem> findByDifficulty(Problem.Difficulty difficulty);
    
    @Query("SELECT p FROM Problem p JOIN p.categories c WHERE c.id = :categoryId")
    List<Problem> findByCategoryId(@Param("categoryId") Long categoryId);
    
    @Query("SELECT p FROM Problem p WHERE p.status = 'ACTIVE'")
    List<Problem> findAllActive();
    
    List<Problem> findByCreatorId(Long creatorId);
    
    @Query("SELECT p FROM Problem p LEFT JOIN FETCH p.categories WHERE p.id = :id")
    Optional<Problem> findByIdWithCategories(@Param("id") Long id);
    
    @Query("SELECT p FROM Problem p LEFT JOIN FETCH p.testCases WHERE p.id = :id")
    Optional<Problem> findByIdWithTestCases(@Param("id") Long id);

    //List<Problem> searchProblems(String query, String difficulty, Long categoryId);
    
    // REMOVE or COMMENT OUT this method - it causes MultipleBagFetchException
    // @Query("SELECT p FROM Problem p LEFT JOIN FETCH p.categories LEFT JOIN FETCH p.testCases WHERE p.id = :id")
    // Optional<Problem> findByIdWithCategoriesAndTestCases(@Param("id") Long id);
}