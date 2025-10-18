package com.CodeForge.CodeForge.services;

import com.CodeForge.CodeForge.model.*;
import com.CodeForge.CodeForge.model.Problem.Difficulty;
import com.CodeForge.CodeForge.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.HashSet;

import com.CodeForge.CodeForge.dto.*;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ProblemService {

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
        } catch (Exception e) {
            throw new RuntimeException("Invalid test case format: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public List<Problem> getAllProblems() {
        List<Problem> problems = problemRepository.findAllActiveWithDetails();
        
        // Force initialization of lazy collections within transaction
        problems.forEach(problem -> {
            problem.getCodeTemplates().size();
            problem.getTestCases().size();
            problem.getCategories().size();
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

        // Delete related entities first
        submissionRepository.deleteByProblemId(problemId);
        contestProblemRepository.deleteByProblem(problem);

        // Delete user progress entries
        userProgressRepository.deleteById(problemId);

        codeTemplateRepository.deleteByProblemId(problemId);
        testCaseRepository.deleteByProblemId(problemId);

        problemRepository.delete(problem);
    }

    public void deleteProblem(Long problemId) {
        deleteProblem(problemId, null); // Call with null userId for admin override
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
}
