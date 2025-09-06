package com.codeforge.codeforge.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.codeforge.codeforge.model.Problem;
import com.codeforge.codeforge.model.Submission;
import com.codeforge.codeforge.model.User;

@Repository
public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    
    // Find submissions by user
    List<Submission> findByUser(User user);
    Page<Submission> findByUser(User user, Pageable pageable);
    
    // Find submissions by problem
    List<Submission> findByProblem(Problem problem);
    Page<Submission> findByProblem(Problem problem, Pageable pageable);
    
    // Find submissions by user and problem
    List<Submission> findByUserAndProblem(User user, Problem problem);
    Page<Submission> findByUserAndProblem(User user, Problem problem, Pageable pageable);
    
    // Find submissions by status
    List<Submission> findByStatus(Submission.Status status);
    List<Submission> findByStatusIn(List<Submission.Status> statuses);
    
    // Find pending submissions (for processing)
    List<Submission> findByStatus(Submission.Status status, Pageable pageable);
    
    // Find submissions by language
    List<Submission> findByLanguage(Submission.Language language);
    
    // Find accepted submissions
    List<Submission> findByUserAndStatus(User user, Submission.Status status);
    List<Submission> findByProblemAndStatus(Problem problem, Submission.Status status);
    List<Submission> findByUserAndProblemAndStatus(User user, Problem problem, Submission.Status status);
    
    // Find best submission (fastest execution for accepted solutions)
    @Query("SELECT s FROM Submission s WHERE s.user = :user AND s.problem = :problem AND s.status = 'ACCEPTED' ORDER BY s.executionTime ASC")
    Optional<Submission> findBestSubmission(@Param("user") User user, @Param("problem") Problem problem);
    
    // Find latest submission for a user and problem
    @Query("SELECT s FROM Submission s WHERE s.user = :user AND s.problem = :problem ORDER BY s.submittedAt DESC")
    List<Submission> findLatestSubmissions(@Param("user") User user, @Param("problem") Problem problem, Pageable pageable);
    
    // Count operations
    long countByUser(User user);
    long countByProblem(Problem problem);
    long countByUserAndStatus(User user, Submission.Status status);
    long countByProblemAndStatus(Problem problem, Submission.Status status);
    long countByUserAndProblem(User user, Problem problem);
    long countByUserAndProblemAndStatus(User user, Problem problem, Submission.Status status);
    
    // Statistics
    @Query("SELECT COUNT(s) FROM Submission s WHERE s.user = :user AND s.status = 'ACCEPTED'")
    long countAcceptedSubmissions(@Param("user") User user);
    
    @Query("SELECT COUNT(DISTINCT s.problem) FROM Submission s WHERE s.user = :user AND s.status = 'ACCEPTED'")
    long countSolvedProblems(@Param("user") User user);
    
    @Query("SELECT AVG(s.executionTime) FROM Submission s WHERE s.problem = :problem AND s.status = 'ACCEPTED' AND s.executionTime IS NOT NULL")
    Double findAverageExecutionTime(@Param("problem") Problem problem);
    
    @Query("SELECT MIN(s.executionTime) FROM Submission s WHERE s.problem = :problem AND s.status = 'ACCEPTED' AND s.executionTime IS NOT NULL")
    Integer findBestExecutionTime(@Param("problem") Problem problem);
    
    // Time-based queries
    List<Submission> findBySubmittedAtAfter(LocalDateTime date);
    List<Submission> findBySubmittedAtBefore(LocalDateTime date);
    List<Submission> findBySubmittedAtBetween(LocalDateTime start, LocalDateTime end);
    
    // Find submissions for a user in time range
    List<Submission> findByUserAndSubmittedAtBetween(User user, LocalDateTime start, LocalDateTime end);
    
    // Find top performers for a problem
    @Query("SELECT s FROM Submission s WHERE s.problem = :problem AND s.status = 'ACCEPTED' ORDER BY s.executionTime ASC, s.memoryUsed ASC")
    List<Submission> findTopPerformers(@Param("problem") Problem problem, Pageable pageable);
    
    // Check if user has solved problem
    @Query("SELECT COUNT(s) > 0 FROM Submission s WHERE s.user = :user AND s.problem = :problem AND s.status = 'ACCEPTED'")
    boolean hasUserSolvedProblem(@Param("user") User user, @Param("problem") Problem problem);
    
    // Check if user has attempted problem
    @Query("SELECT COUNT(s) > 0 FROM Submission s WHERE s.user = :user AND s.problem = :problem")
    boolean hasUserAttemptedProblem(@Param("user") User user, @Param("problem") Problem problem);
    
    // Find submissions with high memory usage (for analysis)
    @Query("SELECT s FROM Submission s WHERE s.memoryUsed > :threshold ORDER BY s.memoryUsed DESC")
    List<Submission> findHighMemorySubmissions(@Param("threshold") Integer threshold, Pageable pageable);
    
    // Find submissions with long execution time (for analysis)
    @Query("SELECT s FROM Submission s WHERE s.executionTime > :threshold ORDER BY s.executionTime DESC")
    List<Submission> findSlowSubmissions(@Param("threshold") Integer threshold, Pageable pageable);
}