package com.codeforge.codeforge.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.codeforge.codeforge.model.CodeTemplate;
import com.codeforge.codeforge.model.Problem;

@Repository
public interface CodeTemplateRepository extends JpaRepository<CodeTemplate, Long> {

    // Find templates for a given problem
    List<CodeTemplate> findByProblem(Problem problem);

    // Find template by problem + language (unique constraint)
    Optional<CodeTemplate> findByProblemAndLanguage(Problem problem, CodeTemplate.Language language);

    // Check if a template exists for a problem + language
    boolean existsByProblemAndLanguage(Problem problem, CodeTemplate.Language language);

    // Delete all templates for a problem
    void deleteByProblem(Problem problem);

    // Search template by language (optional, useful for filtering)
    List<CodeTemplate> findByLanguage(CodeTemplate.Language language);

    // Get template code only (useful for performance)
    @Query("SELECT ct.templateCode FROM CodeTemplate ct WHERE ct.problem = :problem AND ct.language = :language")
    Optional<String> findTemplateCode(
            @Param("problem") Problem problem,
            @Param("language") CodeTemplate.Language language
    );
}
