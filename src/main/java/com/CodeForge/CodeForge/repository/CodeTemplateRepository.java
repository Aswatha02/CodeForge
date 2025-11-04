package com.CodeForge.CodeForge.repository;

import com.CodeForge.CodeForge.model.CodeTemplate;
import com.CodeForge.CodeForge.model.Problem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CodeTemplateRepository extends JpaRepository<CodeTemplate, Long> {

    List<CodeTemplate> findByProblem(Problem problem);
    
    Optional<CodeTemplate> findByProblemAndLanguage(Problem problem, CodeTemplate.Language language);
    
    @Modifying
    @Query("DELETE FROM CodeTemplate ct WHERE ct.problem.id = :problemId")
    void deleteByProblemId(@Param("problemId") Long problemId);
    
    @Query("SELECT ct FROM CodeTemplate ct WHERE ct.problem.id = :problemId")
    List<CodeTemplate> findByProblemId(@Param("problemId") Long problemId);
}