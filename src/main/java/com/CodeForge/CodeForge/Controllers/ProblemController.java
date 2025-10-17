package com.CodeForge.CodeForge.Controllers;

import com.CodeForge.CodeForge.dto.ProblemResponseDTO;
import com.CodeForge.CodeForge.dto.ProblemRequest;
import com.CodeForge.CodeForge.model.Problem;
import com.CodeForge.CodeForge.model.Submission;
import com.CodeForge.CodeForge.model.Category;
import com.CodeForge.CodeForge.model.TestCase;
import com.CodeForge.CodeForge.model.CodeTemplate;
import com.CodeForge.CodeForge.services.ProblemService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.CodeForge.CodeForge.dto.SubmissionRequest;
import com.CodeForge.CodeForge.services.SubmissionService;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.CodeForge.CodeForge.model.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@RestController
@RequestMapping("/api/problems")
@CrossOrigin(origins = "http://localhost:3000")
public class ProblemController {
    
    private final ProblemService problemService;
    private final SubmissionService submissionService;

    // Manual constructor since Lombok is not working
    public ProblemController(ProblemService problemService, SubmissionService submissionService) {
        this.problemService = problemService;
        this.submissionService = submissionService;
    }

    @PostMapping
    public ResponseEntity<?> createProblem(
            @RequestBody ProblemRequest problemRequest,
            @RequestParam Long creatorId) {
        try {
            System.out.println("=== CREATING PROBLEM ===");
            System.out.println("Title: " + problemRequest.getTitle());
            System.out.println("Code Templates: " + (problemRequest.getCodeTemplates() != null ? problemRequest.getCodeTemplates().size() : 0));
            System.out.println("Test Cases: " + (problemRequest.getTestCases() != null ? problemRequest.getTestCases().size() : 0));

            // Convert ProblemRequest to Problem entity
            Problem problem = new Problem();
            problem.setTitle(problemRequest.getTitle());
            problem.setSlug(problemRequest.getSlug());
            problem.setDescription(problemRequest.getDescription());
            problem.setInputFormat(problemRequest.getInputFormat());
            problem.setOutputFormat(problemRequest.getOutputFormat());
            problem.setTimeLimitMs(problemRequest.getTimeLimitMs());
            problem.setMemoryLimitMb(problemRequest.getMemoryLimitMb());
            problem.setDifficulty(problemRequest.getDifficulty());

            // Convert categories
            if (problemRequest.getCategoryIds() != null) {
                Set<Category> categories = problemRequest.getCategoryIds().stream()
                    .map(id -> {
                        Category cat = new Category();
                        cat.setId(id);
                        return cat;
                    })
                    .collect(Collectors.toSet());
                problem.setCategories(categories);
            }

            // Convert test cases
            if (problemRequest.getTestCases() != null) {
                List<TestCase> testCases = problemRequest.getTestCases().stream()
                    .map(tc -> {
                        TestCase testCase = new TestCase();
                        testCase.setInputData(tc.getInputData());
                        testCase.setExpectedOutput(tc.getExpectedOutput());
                        testCase.setIsSample(tc.getIsSample());
                        testCase.setTestCaseName(tc.getTestCaseName());
                        testCase.setExplanation(tc.getExplanation());
                        return testCase;
                    })
                    .collect(Collectors.toList());
                problem.setTestCases(testCases);
            }

            // Convert code templates
            if (problemRequest.getCodeTemplates() != null) {
                List<CodeTemplate> codeTemplates = problemRequest.getCodeTemplates().stream()
                    .map(ct -> {
                        CodeTemplate codeTemplate = new CodeTemplate();
                        codeTemplate.setLanguage(CodeTemplate.Language.valueOf(ct.getLanguage()));
                        codeTemplate.setTemplateCode(ct.getTemplateCode());
                        return codeTemplate;
                    })
                    .collect(Collectors.toList());
                problem.setCodeTemplates(codeTemplates);
            }

            Problem createdProblem = problemService.createProblem(problem, creatorId);
            ProblemResponseDTO responseDTO = new ProblemResponseDTO(createdProblem);
            return ResponseEntity.ok(responseDTO);
        } catch (RuntimeException e) {
            System.err.println("Error creating problem: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAllProblems() {
        try {
            List<Problem> problems = problemService.getAllProblems();
            List<ProblemResponseDTO> responseDTOs = problems.stream()
                    .map(ProblemResponseDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responseDTOs);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/search")
    public ResponseEntity<?> searchProblems(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) Long categoryId) {
        try {
            List<Problem> problems = problemService.searchProblems(query, difficulty, categoryId);
            List<ProblemResponseDTO> responseDTOs = problems.stream()
                    .map(ProblemResponseDTO::new)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(responseDTOs);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProblemById(@PathVariable Long id) {
        try {
            System.out.println("=== GET PROBLEM BY ID CALLED ===");
            System.out.println("Requested problem ID: " + id);
            
            Problem problem = problemService.getProblemById(id)
                    .orElseThrow(() -> new RuntimeException("Problem not found"));
            
            System.out.println("✅ PROBLEM FOUND: " + problem.getTitle());
            System.out.println("Problem ID: " + problem.getId());
            System.out.println("Problem Slug: " + problem.getSlug());
            System.out.println("Problem Status: " + problem.getStatus());
            
            // Debug logging for associations
            System.out.println("Categories count: " + (problem.getCategories() != null ? problem.getCategories().size() : 0));
            System.out.println("Code templates count: " + (problem.getCodeTemplates() != null ? problem.getCodeTemplates().size() : 0));
            System.out.println("Test cases count: " + (problem.getTestCases() != null ? problem.getTestCases().size() : 0));
            
            ProblemResponseDTO responseDTO = new ProblemResponseDTO(problem);
            System.out.println("✅ DTO created successfully, returning response");
            
            return ResponseEntity.ok(responseDTO);
        } catch (RuntimeException e) {
            System.err.println("❌ ERROR in getProblemById: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error: " + e.getMessage()));
        }
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<?> getProblemBySlug(@PathVariable String slug) {
        try {
            Problem problem = problemService.getProblemBySlug(slug)
                    .orElseThrow(() -> new RuntimeException("Problem not found"));
            ProblemResponseDTO responseDTO = new ProblemResponseDTO(problem);
            return ResponseEntity.ok(responseDTO);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<?> submitSolution(
            @PathVariable Long id,
            @RequestBody SubmissionRequest submissionRequest,
            @AuthenticationPrincipal User user) {
        try {
            Submission submission = submissionService.submitCode(
                id, 
                null,
                user, 
                submissionRequest.getCode(), 
                submissionRequest.getLanguage()
            );
            return ResponseEntity.ok(submission);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<?> deleteProblem(@PathVariable Long id, @AuthenticationPrincipal User user) {
        try {
            // Check if user is admin
            if (user.getRole() != User.Role.ADMIN) {
                return ResponseEntity.status(403).body(Map.of("error", "Access denied"));
            }

            problemService.deleteProblem(id, user.getId());
            return ResponseEntity.ok().build();
            
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}