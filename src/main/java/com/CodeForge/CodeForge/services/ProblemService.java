package com.CodeForge.CodeForge.services;

import com.CodeForge.CodeForge.model.*;
import com.CodeForge.CodeForge.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import com.fasterxml.jackson.databind.JsonNode;

@Data
@Service
public class ProblemService {

    private final ProblemRepository problemRepository;
    private final CategoryRepository categoryRepository;
    private final TestCaseRepository testCaseRepository;
    private final CodeTemplateRepository codeTemplateRepository;
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final ContestProblemRepository contestProblemRepository;
    private final UserProgressRepository userProgressRepository;

    @PersistenceContext
    private EntityManager entityManager;

    // Manually created constructor for dependency injection
    public ProblemService(
            ProblemRepository problemRepository,
            CategoryRepository categoryRepository,
            TestCaseRepository testCaseRepository,
            CodeTemplateRepository codeTemplateRepository,
            UserRepository userRepository,
            SubmissionRepository submissionRepository,
            ContestProblemRepository contestProblemRepository,
            UserProgressRepository userProgressRepository
    ) {
        this.problemRepository = problemRepository;
        this.categoryRepository = categoryRepository;
        this.testCaseRepository = testCaseRepository;
        this.codeTemplateRepository = codeTemplateRepository;
        this.userRepository = userRepository;
        this.submissionRepository = submissionRepository;
        this.contestProblemRepository = contestProblemRepository;
        this.userProgressRepository = userProgressRepository;
    }

    @Transactional
    public Problem createProblem(Problem problem, Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Creator not found"));
        
        if (problemRepository.findBySlug(problem.getSlug()).isPresent()) {
            throw new RuntimeException("Problem slug already exists");
        }
        
        problem.setCreator(creator);
        
        // Save problem first
        Problem savedProblem = problemRepository.save(problem);
        
        // Save code templates
        if (problem.getCodeTemplates() != null && !problem.getCodeTemplates().isEmpty()) {
            for (CodeTemplate codeTemplate : problem.getCodeTemplates()) {
                codeTemplate.setProblem(savedProblem);
                codeTemplateRepository.save(codeTemplate);
            }
        }
        
        // Save test cases
        if (problem.getTestCases() != null && !problem.getTestCases().isEmpty()) {
            for (TestCase testCase : problem.getTestCases()) {
                testCase.setProblem(savedProblem);
                
                // Validate test case JSON format
                validateTestCaseFormat(testCase.getInputData(), testCase.getExpectedOutput());
                
                testCaseRepository.save(testCase);
            }
        }
        
        return savedProblem;
    }

    private void validateTestCaseFormat(String inputData, String expectedOutput) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            
            // Validate input is proper JSON
            JsonNode inputNode = mapper.readTree(inputData);
            
            // Validate expected output format
            JsonNode outputNode = mapper.readTree(expectedOutput);
            
        } catch (Exception e) {
            throw new RuntimeException("Invalid test case format: " + e.getMessage());
        }
    }

    public List<Problem> getAllProblems() {
        return problemRepository.findAllActive();
    }

    public Optional<Problem> getProblemById(Long id) {
        Optional<Problem> problemOpt = problemRepository.findById(id);
        
        if (problemOpt.isPresent()) {
            Problem problem = problemOpt.get();
            
            List<CodeTemplate> codeTemplates = codeTemplateRepository.findByProblem(problem);
            problem.setCodeTemplates(codeTemplates);
            
            List<TestCase> testCases = testCaseRepository.findByProblem(problem);
            problem.setTestCases(testCases);
            
            return Optional.of(problem);
        }
        
        return Optional.empty();
    }

    public Optional<Problem> getProblemBySlug(String slug) {
        Optional<Problem> problemOpt = problemRepository.findBySlug(slug);
        
        if (problemOpt.isPresent()) {
            Problem problem = problemOpt.get();
            
            List<CodeTemplate> codeTemplates = codeTemplateRepository.findByProblem(problem);
            problem.setCodeTemplates(codeTemplates);
            
            return Optional.of(problem);
        }
        
        return Optional.empty();
    }

    public List<Problem> getProblemsByCategory(Long categoryId) {
        return problemRepository.findByCategoryId(categoryId);
    }

    public List<Problem> getProblemsByDifficulty(Problem.Difficulty difficulty) {
        return problemRepository.findByDifficulty(difficulty);
    }

    @Transactional
    public Problem updateProblem(Long problemId, Problem updatedProblem, Long userId) {
        Problem existingProblem = problemRepository.findById(problemId)
                .orElseThrow(() -> new RuntimeException("Problem not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!existingProblem.getCreator().getId().equals(userId) && !user.getRole().equals("ADMIN")) {
            throw new RuntimeException("Not authorized to update this problem");
        }
        
        existingProblem.setTitle(updatedProblem.getTitle());
        existingProblem.setDescription(updatedProblem.getDescription());
        existingProblem.setInputFormat(updatedProblem.getInputFormat());
        existingProblem.setOutputFormat(updatedProblem.getOutputFormat());
        existingProblem.setDifficulty(updatedProblem.getDifficulty());
        existingProblem.setTimeLimitMs(updatedProblem.getTimeLimitMs());
        existingProblem.setMemoryLimitMb(updatedProblem.getMemoryLimitMb());
        
        if (updatedProblem.getCategories() != null) {
            existingProblem.setCategories(updatedProblem.getCategories());
        }
        
        if (updatedProblem.getCodeTemplates() != null) {
            codeTemplateRepository.deleteByProblemId(problemId);
            
            for (CodeTemplate codeTemplate : updatedProblem.getCodeTemplates()) {
                codeTemplate.setProblem(existingProblem);
                codeTemplateRepository.save(codeTemplate);
            }
        }
        
        if (updatedProblem.getTestCases() != null) {
            testCaseRepository.deleteByProblemId(problemId);
            
            for (TestCase testCase : updatedProblem.getTestCases()) {
                testCase.setProblem(existingProblem);
                testCaseRepository.save(testCase);
            }
        }
        
        return problemRepository.save(existingProblem);
    }

    @Transactional
    public void deleteProblem(Long problemId, Long userId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new RuntimeException("Problem not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!problem.getCreator().getId().equals(userId) && user.getRole() != User.Role.ADMIN) {
            throw new RuntimeException("Not authorized to delete this problem");
        }

        // Delete related entities first to avoid foreign key constraint violations

        // Delete submissions related to this problem
        submissionRepository.deleteByProblemId(problemId);

        // Delete contest problems related to this problem
        contestProblemRepository.deleteByProblem(problem);

        // Delete code templates and test cases
        codeTemplateRepository.deleteByProblemId(problemId);
        testCaseRepository.deleteByProblemId(problemId);

        // Finally delete the problem
        problemRepository.delete(problem);
    }

    public List<Problem> searchProblems(String query, String difficulty, Long categoryId) {
        List<Problem> allProblems = getAllProblems();
        
        return allProblems.stream()
                .filter(problem -> 
                    (query == null || query.isEmpty() || 
                     problem.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                     problem.getDescription().toLowerCase().contains(query.toLowerCase())) &&
                    (difficulty == null || difficulty.isEmpty() || 
                     problem.getDifficulty().name().equalsIgnoreCase(difficulty)) &&
                    (categoryId == null || 
                     problem.getCategories().stream().anyMatch(cat -> cat.getId().equals(categoryId)))
                )
                .toList();
    }
}
