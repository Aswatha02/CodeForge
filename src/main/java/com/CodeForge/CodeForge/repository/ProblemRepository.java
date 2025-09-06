package com.codeforge.codeforge.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.codeforge.codeforge.model.Problem;
import com.codeforge.codeforge.model.User;

@Repository
public interface ProblemRepository extends JpaRepository<Problem, Long> {
    
    // Find by slug (for pretty URLs)
    Optional<Problem> findBySlug(String slug);
    
    // Find by difficulty
    List<Problem> findByDifficulty(Problem.Difficulty difficulty);
    Page<Problem> findByDifficulty(Problem.Difficulty difficulty, Pageable pageable);
    
    // Find by status
    List<Problem> findByStatus(Problem.Status status);
    Page<Problem> findByStatus(Problem.Status status, Pageable pageable);
    
    // Find by difficulty and status
    List<Problem> findByDifficultyAndStatus(Problem.Difficulty difficulty, Problem.Status status);
    Page<Problem> findByDifficultyAndStatus(Problem.Difficulty difficulty, Problem.Status status, Pageable pageable);
    
    // Find by creator
    List<Problem> findByCreatedBy(User createdBy);
    Page<Problem> findByCreatedBy(User createdBy, Pageable pageable);
    
    // Find by creator and status
    List<Problem> findByCreatedByAndStatus(User createdBy, Problem.Status status);
    
    // Find active problems
    List<Problem> findByStatusOrderByCreatedAtDesc(Problem.Status status);
    
    // Find problems with minimum acceptance rate
    List<Problem> findByAcceptanceRateGreaterThanEqual(Double minAcceptanceRate);
    
    // Find problems with minimum likes
    List<Problem> findByLikesGreaterThanEqual(Integer minLikes);
    
    // Find problems with minimum submission count
    List<Problem> findBySubmissionCountGreaterThanEqual(Integer minSubmissions);
    
    // Search problems by title
    @Query("SELECT p FROM Problem p WHERE LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) AND p.status = 'ACTIVE'")
    List<Problem> searchByTitle(@Param("query") String query);
    
    // Search problems by title or description
    @Query("SELECT p FROM Problem p WHERE (LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%'))) AND p.status = 'ACTIVE'")
    List<Problem> searchByTitleOrDescription(@Param("query") String query);
    
    // Get problems ordered by creation date (newest first)
    List<Problem> findAllByOrderByCreatedAtDesc();
    Page<Problem> findAllByOrderByCreatedAtDesc(Pageable pageable);
    
    // Get problems ordered by popularity (likes descending)
    List<Problem> findAllByOrderByLikesDesc();
    Page<Problem> findAllByOrderByLikesDesc(Pageable pageable);
    
    // Get problems ordered by acceptance rate (easiest first)
    List<Problem> findAllByOrderByAcceptanceRateDesc();
    
    // Get problems ordered by difficulty
    @Query("SELECT p FROM Problem p WHERE p.status = 'ACTIVE' ORDER BY " +
           "CASE p.difficulty WHEN 'EASY' THEN 1 WHEN 'MEDIUM' THEN 2 WHEN 'HARD' THEN 3 END")
    List<Problem> findAllActiveOrderByDifficulty();
    
    // Count operations
    long countByDifficulty(Problem.Difficulty difficulty);
    long countByStatus(Problem.Status status);
    long countByDifficultyAndStatus(Problem.Difficulty difficulty, Problem.Status status);
    long countByCreatedBy(User createdBy);
    
    // Statistics
    @Query("SELECT AVG(p.acceptanceRate) FROM Problem p WHERE p.status = 'ACTIVE'")
    Double findAverageAcceptanceRate();
    
    @Query("SELECT COUNT(p) FROM Problem p WHERE p.submissionCount > 0")
    Long countProblemsWithSubmissions();
    
    // Find trending problems (most submissions recently)
    @Query("SELECT p FROM Problem p WHERE p.status = 'ACTIVE' ORDER BY p.submissionCount DESC")
    List<Problem> findTrendingProblems(Pageable pageable);
    
    // Find recommended problems for a user (based on difficulty)
    @Query("SELECT p FROM Problem p WHERE p.difficulty = :difficulty AND p.status = 'ACTIVE' " +
           "AND p.id NOT IN (SELECT s.problem.id FROM Submission s WHERE s.user = :user) " +
           "ORDER BY p.acceptanceRate DESC")
    List<Problem> findRecommendedProblems(@Param("user") User user, 
                                        @Param("difficulty") Problem.Difficulty difficulty,
                                        Pageable pageable);
    
    // Check if slug exists
    boolean existsBySlug(String slug);
    
    // Check if user has created problems
    boolean existsByCreatedBy(User createdBy);
    
    // Find problems with similar titles
    @Query("SELECT p FROM Problem p WHERE SOUNDEX(p.title) = SOUNDEX(:title) AND p.id != :excludeId")
    List<Problem> findSimilarProblems(@Param("title") String title, @Param("excludeId") Long excludeId);
}