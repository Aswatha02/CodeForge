package com.codeforge.codeforge.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.codeforge.codeforge.model.Problem;
import com.codeforge.codeforge.model.TestCase;

@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
    
    // Find all test cases for a specific problem
    List<TestCase> findByProblem(Problem problem);
    
    // Find test cases by sample status for a problem
    List<TestCase> findByProblemAndIsSample(Problem problem, Boolean isSample);
    
    // Find test cases by hidden status for a problem
    List<TestCase> findByProblemAndIsHidden(Problem problem, Boolean isHidden);
    
    // Find sample test cases for a problem
    @Query("SELECT tc FROM TestCase tc WHERE tc.problem = :problem AND tc.isSample = true")
    List<TestCase> findSampleCases(@Param("problem") Problem problem);
    
    // Find hidden test cases for a problem
    @Query("SELECT tc FROM TestCase tc WHERE tc.problem = :problem AND tc.isHidden = true")
    List<TestCase> findHiddenCases(@Param("problem") Problem problem);
    
    // Find visible test cases (not hidden) for a problem
    @Query("SELECT tc FROM TestCase tc WHERE tc.problem = :problem AND tc.isHidden = false")
    List<TestCase> findVisibleCases(@Param("problem") Problem problem);
    
    // Find test cases that require exact match
    @Query("SELECT tc FROM TestCase tc WHERE tc.problem = :problem")
    List<TestCase> findByProblemForEvaluation(@Param("problem") Problem problem);
    
    // Count test cases for a problem
    long countByProblem(Problem problem);
    
    // Count sample test cases for a problem
    long countByProblemAndIsSample(Problem problem, Boolean isSample);
    
    // Count hidden test cases for a problem
    long countByProblemAndIsHidden(Problem problem, Boolean isHidden);
    
    // Check if a problem has any test cases
    boolean existsByProblem(Problem problem);
    
    // Check if a problem has sample test cases
    boolean existsByProblemAndIsSample(Problem problem, Boolean isSample);
    
    // Find test cases with explanations
    @Query("SELECT tc FROM TestCase tc WHERE tc.problem = :problem AND tc.explanation IS NOT NULL")
    List<TestCase> findTestCasesWithExplanation(@Param("problem") Problem problem);
    
    // Find test cases created after a certain date
    List<TestCase> findByCreatedAtAfter(java.time.LocalDateTime date);
    
    // Find test cases updated recently
    List<TestCase> findByUpdatedAtAfter(java.time.LocalDateTime date);
    
    // Delete all test cases for a problem (useful for cleanup)
    void deleteByProblem(Problem problem);
    
    // Delete sample test cases for a problem
    void deleteByProblemAndIsSample(Problem problem, Boolean isSample);
    
    // Delete hidden test cases for a problem
    void deleteByProblemAndIsHidden(Problem problem, Boolean isHidden);
}