package com.CodeForge.CodeForge.services;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.CodeForge.CodeForge.dto.CodeTemplateRequest;
import com.CodeForge.CodeForge.dto.ProblemRequest;
import com.CodeForge.CodeForge.dto.TestCaseRequest;
import com.CodeForge.CodeForge.model.Category;
import com.CodeForge.CodeForge.model.CodeTemplate;
import com.CodeForge.CodeForge.model.Problem;
import com.CodeForge.CodeForge.model.Problem.Difficulty;
import com.CodeForge.CodeForge.model.TestCase;
import com.CodeForge.CodeForge.model.User;
import com.CodeForge.CodeForge.repository.CategoryRepository;
import com.CodeForge.CodeForge.repository.CodeTemplateRepository;
import com.CodeForge.CodeForge.repository.ContestProblemRepository;
import com.CodeForge.CodeForge.repository.ProblemRepository;
import com.CodeForge.CodeForge.repository.SubmissionRepository;
import com.CodeForge.CodeForge.repository.TestCaseRepository;
import com.CodeForge.CodeForge.repository.UserProgressRepository;
import com.CodeForge.CodeForge.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ProblemService {

    private static final Logger logger = LoggerFactory.getLogger(ProblemService.class);

    private final ProblemRepository problemRepository;
    private final TestCaseRepository testCaseRepository;
    private final CodeTemplateRepository codeTemplateRepository;
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final ContestProblemRepository contestProblemRepository;
    private final UserProgressRepository userProgressRepository;
    private final CategoryRepository categoryRepository;

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
        this.testCaseRepository = testCaseRepository;
        this.codeTemplateRepository = codeTemplateRepository;
        this.userRepository = userRepository;
        this.submissionRepository = submissionRepository;
        this.contestProblemRepository = contestProblemRepository;
        this.userProgressRepository = userProgressRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public Problem createProblem(Problem problem, Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Creator not found"));
        
        if (problemRepository.findBySlug(problem.getSlug()).isPresent()) {
            throw new RuntimeException("Problem slug already exists");
        }
        
        problem.setCreator(creator);
        
        Problem savedProblem = problemRepository.save(problem);
        
        if (problem.getCodeTemplates() != null && !problem.getCodeTemplates().isEmpty()) {
            for (CodeTemplate codeTemplate : problem.getCodeTemplates()) {
                codeTemplate.setProblem(savedProblem);
                codeTemplateRepository.save(codeTemplate);
            }
        }
        
        if (problem.getTestCases() != null && !problem.getTestCases().isEmpty()) {
            for (TestCase testCase : problem.getTestCases()) {
                testCase.setProblem(savedProblem);
                validateTestCaseFormat(testCase.getInputData(), testCase.getExpectedOutput());
                testCaseRepository.save(testCase);
            }
        }
        
        return savedProblem;
    }

    private void validateTestCaseFormat(String inputData, String expectedOutput) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.readTree(inputData);
            mapper.readTree(expectedOutput);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Invalid test case format: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
public List<Problem> getAllProblems() {
    // Use the method that has @EntityGraph to load test cases
    List<Problem> problems = problemRepository.findAllActiveWithDetails();
    
    System.out.println("DEBUG: Found " + problems.size() + " active problems");
    
    // Debug: Check if test cases are loaded
    problems.forEach(problem -> {
        System.out.println("Problem: " + problem.getTitle());
        System.out.println("Test cases count: " + (problem.getTestCases() != null ? problem.getTestCases().size() : 0));
        if (problem.getTestCases() != null) {
            problem.getTestCases().forEach(tc -> {
                System.out.println("  - Test case: " + tc.getInputData() + " -> " + tc.getExpectedOutput() + " (Sample: " + tc.getIsSample() + ")");
            });
        }
    });
    
    return problems;
}

    @Transactional(readOnly = true)
    public Optional<Problem> getProblemById(Long id) {
        Optional<Problem> problemOpt = problemRepository.findByIdWithDetails(id);
        
        if (problemOpt.isPresent()) {
            Problem problem = problemOpt.get();
            
            // Force initialization of collections
            problem.getCodeTemplates().size();
            problem.getTestCases().size();
            problem.getCategories().size();
            
            return Optional.of(problem);
        }
        
        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public Optional<Problem> getProblemBySlug(String slug) {
        Optional<Problem> problemOpt = problemRepository.findBySlugWithDetails(slug);
        
        if (problemOpt.isPresent()) {
            Problem problem = problemOpt.get();
            
            // Force initialization of collections
            problem.getCodeTemplates().size();
            problem.getTestCases().size();
            problem.getCategories().size();
            
            return Optional.of(problem);
        }
        
        return Optional.empty();
    }

    @Transactional(readOnly = true)
    public List<Problem> getProblemsByCategory(Long categoryId) {
        return problemRepository.findByCategoryId(categoryId);
    }

    @Transactional(readOnly = true)
    public List<Problem> getProblemsByDifficulty(Problem.Difficulty difficulty) {
        return problemRepository.findByDifficulty(difficulty);
    }

    @Transactional
    public Problem updateProblem(Long problemId, Problem updatedProblem, Long userId) {
        Problem existingProblem = problemRepository.findById(problemId)
                .orElseThrow(() -> new RuntimeException("Problem not found"));
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        if (!existingProblem.getCreator().getId().equals(userId) && user.getRole() != User.Role.ADMIN) {
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

        // If userId is null, it's an admin override - skip authorization check
        if (userId != null) {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (!problem.getCreator().getId().equals(userId) && user.getRole() != User.Role.ADMIN) {
                throw new RuntimeException("Not authorized to delete this problem");
            }
        }

        // Clear many-to-many relationships first to avoid constraint issues
        problem.getCategories().clear();
        problemRepository.save(problem);

        // Delete related entities in the correct order
        // Delete contest problems first
        contestProblemRepository.deleteByProblem(problem);

        // Delete submissions (this will cascade to execution results if any)
        submissionRepository.deleteByProblemId(problemId);

        // Remove this problem from all user progress solvedProblems sets
        userProgressRepository.removeProblemFromSolvedProblems(problemId);

        // Note: codeTemplates and testCases will be deleted by cascade when problem is deleted
        problemRepository.delete(problem);
    }

    @Transactional
    public void deleteProblem(Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new RuntimeException("Problem not found"));

        System.out.println("=== DELETING PROBLEM: " + problem.getTitle() + " (ID: " + problemId + ") ===");
        
        try {
            // 1. First, clear many-to-many relationships
            System.out.println("Clearing categories...");
            problem.getCategories().clear();
            problemRepository.saveAndFlush(problem); // Save immediately to clear relationships

            // 2. Delete related entities in correct order
            System.out.println("Deleting contest problems...");
            contestProblemRepository.deleteByProblem(problem);

            // 3. Delete submissions (this is the critical part)
            System.out.println("Deleting submissions...");
            submissionRepository.deleteByProblemId(problemId);
            System.out.println("Deleted submissions");

            // 4. Remove from user progress
            System.out.println("Removing from user progress...");
            userProgressRepository.removeProblemFromSolvedProblems(problemId);

            // 5. Delete test cases and code templates (should cascade, but do explicitly if needed)
            System.out.println("Deleting test cases...");
            testCaseRepository.deleteByProblemId(problemId);

            System.out.println("Deleting code templates...");
            codeTemplateRepository.deleteByProblemId(problemId);

            // 6. Finally delete the problem
            System.out.println("Deleting problem entity...");
            problemRepository.delete(problem);
            problemRepository.flush(); // Force immediate delete

            System.out.println("=== PROBLEM DELETED SUCCESSFULLY ===");

        } catch (Exception e) {
            System.err.println("=== ERROR DELETING PROBLEM ===");
            System.err.println("Error: " + e.getMessage());
            logger.error("Failed to delete problem with ID: {}", problemId, e);
            throw new RuntimeException("Failed to delete problem: " + e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
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

    public Long getTotalProblems() {
        return problemRepository.count();
    }

    public Long getProblemCountByCategory(Long categoryId) {
        // Simple implementation - count problems that have this category
        List<Problem> problems = getProblemsByCategory(categoryId);
        return (long) problems.size();
    }

    // Simplified method for admin dashboard filtering - removed status filter
    public List<Problem> getProblemsWithFilters(String difficulty, String category) {
        // Handle empty strings as null
        if (difficulty != null && difficulty.trim().isEmpty()) {
            difficulty = null;
        }
        if (category != null && category.trim().isEmpty()) {
            category = null;
        }

        // Your existing filtering logic, but now it checks multiple categories
        if (difficulty != null && category != null) {
            return problemRepository.findByDifficultyAndCategoriesName(
                Difficulty.valueOf(difficulty.toUpperCase()), category);
        } else if (difficulty != null) {
            return problemRepository.findByDifficulty(Difficulty.valueOf(difficulty.toUpperCase()));
        } else if (category != null) {
            return problemRepository.findByCategoriesNameContainingIgnoreCase(category);
        } else {
            return problemRepository.findAll();
        }
    }

    public void addCategoriesToProblem(Long problemId, List<Long> categoryIds) {
        Problem problem = problemRepository.findById(problemId)
            .orElseThrow(() -> new RuntimeException("Problem not found"));
        
        Set<Category> categories = categoryRepository.findAllById(categoryIds)
            .stream()
            .collect(Collectors.toSet());
        
        problem.getCategories().addAll(categories);
        problemRepository.save(problem);
    }

    public void removeCategoriesFromProblem(Long problemId, List<Long> categoryIds) {
        Problem problem = problemRepository.findById(problemId)
            .orElseThrow(() -> new RuntimeException("Problem not found"));

        problem.getCategories().removeIf(category ->
            categoryIds.contains(category.getId()));

        problemRepository.save(problem);
    }

    @Transactional
    public Problem updateProblem(Long problemId, ProblemRequest problemRequest) {
        Problem existingProblem = problemRepository.findById(problemId)
                .orElseThrow(() -> new RuntimeException("Problem not found"));

        // Update basic fields
        existingProblem.setTitle(problemRequest.getTitle());
        existingProblem.setSlug(problemRequest.getSlug());
        existingProblem.setDescription(problemRequest.getDescription());
        existingProblem.setInputFormat(problemRequest.getInputFormat());
        existingProblem.setOutputFormat(problemRequest.getOutputFormat());
        existingProblem.setDifficulty(problemRequest.getDifficulty());
        existingProblem.setTimeLimitMs(problemRequest.getTimeLimitMs());
        existingProblem.setMemoryLimitMb(problemRequest.getMemoryLimitMb());

        // Update additional fields
        existingProblem.setConstraints(problemRequest.getConstraints());
        existingProblem.setPoints(problemRequest.getPoints());
        existingProblem.setTags(problemRequest.getTags());
        existingProblem.setExampleInput(problemRequest.getExampleInput());
        existingProblem.setExampleOutput(problemRequest.getExampleOutput());
        existingProblem.setIsPrivate(problemRequest.getIsPrivate());

        // Update function signature fields
        existingProblem.setFunctionName(problemRequest.getFunctionName());
        existingProblem.setParameters(problemRequest.getParameters());
        existingProblem.setReturnType(problemRequest.getReturnType());

        // Update categories if provided
        if (problemRequest.getCategoryIds() != null && !problemRequest.getCategoryIds().isEmpty()) {
            Set<Category> categories = new HashSet<>();
            for (Long categoryId : problemRequest.getCategoryIds()) {
                Category category = categoryRepository.findById(categoryId).orElse(null);
                if (category != null) {
                    categories.add(category);
                }
            }
            existingProblem.setCategories(categories);
        }

        // Update test cases if provided
        if (problemRequest.getTestCases() != null && !problemRequest.getTestCases().isEmpty()) {
            testCaseRepository.deleteByProblemId(problemId);
            for (TestCaseRequest tcRequest : problemRequest.getTestCases()) {
                TestCase testCase = new TestCase();
                testCase.setInputData(tcRequest.getInputData());
                testCase.setExpectedOutput(tcRequest.getExpectedOutput());
                testCase.setIsSample(tcRequest.getIsSample() != null ? tcRequest.getIsSample() : false);
                testCase.setExplanation(tcRequest.getExplanation());
                testCase.setTestCaseName(tcRequest.getTestCaseName());
                testCase.setProblem(existingProblem);
                testCaseRepository.save(testCase);
            }
        }

        // Update code templates if provided
        if (problemRequest.getCodeTemplates() != null && !problemRequest.getCodeTemplates().isEmpty()) {
            codeTemplateRepository.deleteByProblemId(problemId);
            for (CodeTemplateRequest ctRequest : problemRequest.getCodeTemplates()) {
                CodeTemplate codeTemplate = new CodeTemplate();
                codeTemplate.setLanguage(CodeTemplate.Language.valueOf(ctRequest.getLanguage().toUpperCase()));
                codeTemplate.setTemplateCode(ctRequest.getTemplateCode());
                codeTemplate.setProblem(existingProblem);
                codeTemplateRepository.save(codeTemplate);
            }
        }

        return problemRepository.save(existingProblem);
    }

    @Transactional
    public void updateProblemStatus(Long problemId, boolean published) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new RuntimeException("Problem not found"));
        problem.setStatus(published ? Problem.Status.ACTIVE : Problem.Status.INACTIVE);
        problemRepository.save(problem);
    }

    @Transactional
    public void addTestCase(Long problemId, TestCase testCase) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new RuntimeException("Problem not found"));
        testCase.setProblem(problem);
        validateTestCaseFormat(testCase.getInputData(), testCase.getExpectedOutput());
        testCaseRepository.save(testCase);
    }

    @Transactional
    public void updateTestCase(Long problemId, Long testCaseId, TestCase updatedTestCase) {
        TestCase existingTestCase = testCaseRepository.findById(testCaseId)
                .orElseThrow(() -> new RuntimeException("Test case not found"));
        if (!existingTestCase.getProblem().getId().equals(problemId)) {
            throw new RuntimeException("Test case does not belong to the specified problem");
        }
        existingTestCase.setInputData(updatedTestCase.getInputData());
        existingTestCase.setExpectedOutput(updatedTestCase.getExpectedOutput());
        existingTestCase.setIsSample(updatedTestCase.getIsSample());
        existingTestCase.setExplanation(updatedTestCase.getExplanation());
        existingTestCase.setTestCaseName(updatedTestCase.getTestCaseName());
        validateTestCaseFormat(existingTestCase.getInputData(), existingTestCase.getExpectedOutput());
        testCaseRepository.save(existingTestCase);
    }

    @Transactional
    public void deleteTestCase(Long problemId, Long testCaseId) {
        TestCase testCase = testCaseRepository.findById(testCaseId)
                .orElseThrow(() -> new RuntimeException("Test case not found"));
        if (!testCase.getProblem().getId().equals(problemId)) {
            throw new RuntimeException("Test case does not belong to the specified problem");
        }
        testCaseRepository.delete(testCase);
    }

    @Transactional
    public void bulkUpdateTestCases(Long problemId, List<TestCase> testCases) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new RuntimeException("Problem not found"));
        testCaseRepository.deleteByProblemId(problemId);
        for (TestCase testCase : testCases) {
            testCase.setProblem(problem);
            validateTestCaseFormat(testCase.getInputData(), testCase.getExpectedOutput());
            testCaseRepository.save(testCase);
        }
    }

    @Transactional
    public void addCodeTemplate(Long problemId, CodeTemplate codeTemplate) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new RuntimeException("Problem not found"));
        codeTemplate.setProblem(problem);
        codeTemplateRepository.save(codeTemplate);
    }

    @Transactional
    public void updateCodeTemplate(Long problemId, Long templateId, CodeTemplate updatedCodeTemplate) {
        CodeTemplate existingCodeTemplate = codeTemplateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Code template not found"));
        if (!existingCodeTemplate.getProblem().getId().equals(problemId)) {
            throw new RuntimeException("Code template does not belong to the specified problem");
        }
        existingCodeTemplate.setLanguage(updatedCodeTemplate.getLanguage());
        existingCodeTemplate.setTemplateCode(updatedCodeTemplate.getTemplateCode());
        codeTemplateRepository.save(existingCodeTemplate);
    }

    @Transactional
    public void deleteCodeTemplate(Long problemId, Long templateId) {
        CodeTemplate codeTemplate = codeTemplateRepository.findById(templateId)
                .orElseThrow(() -> new RuntimeException("Code template not found"));
        if (!codeTemplate.getProblem().getId().equals(problemId)) {
            throw new RuntimeException("Code template does not belong to the specified problem");
        }
        codeTemplateRepository.delete(codeTemplate);
    }
}
