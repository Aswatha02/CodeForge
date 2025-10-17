package com.CodeForge.CodeForge.repository;

import com.CodeForge.CodeForge.model.TestCase;
import com.CodeForge.CodeForge.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
@Repository
public interface TestCaseRepository extends JpaRepository<TestCase, Long> {
    
    List<TestCase> findByProblemId(Long problemId);
    List<TestCase> findByProblem(Problem problem);
    List<TestCase> findByProblemIdAndIsSample(Long problemId, Boolean isSample);
    int countByProblemId(Long problemId);
    
    @Modifying
    @Query("DELETE FROM TestCase tc WHERE tc.problem.id = :problemId")
    void deleteByProblemId(@Param("problemId") Long problemId);
}