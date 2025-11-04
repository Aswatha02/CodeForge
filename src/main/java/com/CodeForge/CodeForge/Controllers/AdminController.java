package com.CodeForge.CodeForge.Controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.CodeForge.CodeForge.dto.CodeTemplateRequest;
import com.CodeForge.CodeForge.dto.ProblemRequest;
import com.CodeForge.CodeForge.dto.ProblemResponseDTO;
import com.CodeForge.CodeForge.dto.TestCaseRequest;
import com.CodeForge.CodeForge.model.Category;
import com.CodeForge.CodeForge.model.CodeTemplate;
import com.CodeForge.CodeForge.model.Contest;
import com.CodeForge.CodeForge.model.Problem;
import com.CodeForge.CodeForge.model.Submission;
import com.CodeForge.CodeForge.model.TestCase;
import com.CodeForge.CodeForge.model.User;
import com.CodeForge.CodeForge.services.CategoryService;
import com.CodeForge.CodeForge.services.ContestService;
import com.CodeForge.CodeForge.services.ProblemService;
import com.CodeForge.CodeForge.services.SubmissionService;
import com.CodeForge.CodeForge.services.UserService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private ProblemService problemService;

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ContestService contestService;

    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);

    // ==================== DASHBOARD ENDPOINTS ====================

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getAdminStats() {
        // Temporary: Check authentication manually
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        System.out.println("=== ADMIN STATS ===");
        System.out.println("Authentication: " + auth);
        if (auth != null) {
            System.out.println("User: " + auth.getName());
            System.out.println("Authorities: " + auth.getAuthorities());
        }

        Map<String, Object> stats = new HashMap<>();
        try {
            stats.put("totalProblems", problemService.getTotalProblems());
            stats.put("totalUsers", userService.getTotalUsers());
            stats.put("totalSubmissions", submissionService.getTotalSubmissions());
            stats.put("activeContests", contestService.getActiveContestsCount());
            stats.put("dailyActiveUsers", userService.getDailyActiveUsers());
            stats.put("systemUptime", "99.9%");
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @GetMapping("/activity")
    public ResponseEntity<List<Map<String, Object>>> getRecentActivity() {
        List<Map<String, Object>> activity = new ArrayList<>();

        // Get recent submissions - using existing method or creating a new one
        List<Submission> recentSubmissions = submissionService.getRecentSubmissions();
        for (Submission submission : recentSubmissions) {
            Map<String, Object> activityItem = new HashMap<>();
            activityItem.put("id", submission.getId());
            activityItem.put("user", submission.getUser().getUsername());
            activityItem.put("action", "submitted");
            activityItem.put("problem", submission.getProblem().getTitle());
            activityItem.put("time", formatTimeAgo(submission.getSubmittedAt()));
            activity.add(activityItem);
        }

        // Add some system activities
        Map<String, Object> systemActivity = new HashMap<>();
        systemActivity.put("id", -1);
        systemActivity.put("user", "system");
        systemActivity.put("action", "system_started");
        systemActivity.put("problem", "");
        systemActivity.put("time", "Just now");
        activity.add(0, systemActivity);

        return ResponseEntity.ok(activity);
    }

    @GetMapping("/welcome")
    public ResponseEntity<Map<String, Object>> welcome(HttpServletRequest request) {
        logger.info("Request received: {} {}", request.getMethod(), request.getRequestURI());

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to the CodeForge Admin API!");

        return ResponseEntity.ok(response);
    }

    // ==================== USER MANAGEMENT ENDPOINTS ====================

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) String sortOrder,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        try {
            List<User> users = userService.getUsersWithFilters(search, role, status, sortBy, sortOrder, page, size);
            List<Map<String, Object>> userDTOs = users.stream().map(user -> {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("username", user.getUsername());
                userMap.put("email", user.getEmail());
                userMap.put("role", user.getRole().name());
                userMap.put("status", user.getStatus().name());
                userMap.put("joinDate", user.getCreatedAt() != null ? user.getCreatedAt().toString() : "N/A");
                userMap.put("lastLogin", user.getLastLogin() != null ? user.getLastLogin().toString() : "Never");
                userMap.put("submissionCount", submissionService.getUserSubmissionCount(user.getId()));
                userMap.put("problemsSolved", submissionService.getUserAcceptedSubmissionCount(user.getId()));
                return userMap;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(userDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        try {
            String newRole = request.get("role");
            userService.updateUserRole(userId, newRole);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update user role: " + e.getMessage());
        }
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete user: " + e.getMessage());
        }
    }

    @GetMapping("/users/{userId}/profile")
    public ResponseEntity<Map<String, Object>> getUserProfile(@PathVariable Long userId) {
        try {
            User user = userService.findById(userId);
            if (user == null) {
                return ResponseEntity.notFound().build();
            }

            Map<String, Object> profile = new HashMap<>();
            profile.put("id", user.getId());
            profile.put("username", user.getUsername());
            profile.put("email", user.getEmail());
            profile.put("role", user.getRole().name());
            profile.put("status", user.getStatus().name());
            profile.put("joinDate", user.getCreatedAt() != null ? user.getCreatedAt().toString() : "N/A");
            profile.put("lastLogin", user.getLastLogin() != null ? user.getLastLogin().toString() : "Never");

            // Stats
            profile.put("totalSubmissions", submissionService.getUserSubmissionCount(user.getId()));
            profile.put("problemsSolved", submissionService.getUserAcceptedSubmissionCount(user.getId()));
            profile.put("rank", userService.getUserRank(user.getId()));

            // Recent activity
            List<Map<String, Object>> recentActivity = new ArrayList<>();
            List<Submission> recentSubmissions = submissionService.getRecentSubmissionsByUser(user.getId(), 10);
            for (Submission submission : recentSubmissions) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("id", submission.getId());
                activity.put("problem", submission.getProblem().getTitle());
                activity.put("status", submission.getStatus().name());
                activity.put("time", formatTimeAgo(submission.getSubmittedAt()));
                recentActivity.add(activity);
            }
            profile.put("recentActivity", recentActivity);

            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PutMapping("/users/{userId}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        try {
            String status = request.get("status");
            userService.updateUserStatus(userId, status);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update user status: " + e.getMessage());
        }
    }

    @PutMapping("/users/{userId}/activate")
    public ResponseEntity<?> activateUser(@PathVariable Long userId, @RequestBody Map<String, Object> request) {
        try {
            Boolean activateObj = (Boolean) request.get("activate");
            if (activateObj == null) {
                return ResponseEntity.badRequest().body("Missing or invalid 'activate' parameter");
            }
            boolean activate = Boolean.TRUE.equals(activateObj);
            userService.activateUser(userId, activate);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            if (e.getMessage() != null && e.getMessage().contains("User not found")) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.badRequest().body("Failed to update user activation: " + e.getMessage());
        }
    }

    @PostMapping("/users/{userId}/reset-progress")
    public ResponseEntity<?> resetUserProgress(@PathVariable Long userId) {
        try {
            userService.resetUserProgress(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to reset user progress: " + e.getMessage());
        }
    }

    @PutMapping("/users/{userId}/details")
    public ResponseEntity<?> updateUserDetails(@PathVariable Long userId, @RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String role = request.get("role");
            String password = request.get("password");
            String status = request.get("status");

            userService.updateUserDetails(userId, email, role, password, status);

            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update user details: " + e.getMessage());
        }
    }

    @GetMapping("/users/stats")
    public ResponseEntity<Map<String, Object>> getUserStats() {
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("totalUsers", userService.getTotalUsers());
            stats.put("activeUsers", userService.getActiveUsersCount());
            stats.put("bannedUsers", userService.getBannedUsersCount());
            stats.put("newUsersThisWeek", userService.getNewUsersThisWeek());
            stats.put("newUsersThisMonth", userService.getNewUsersThisMonth());
            stats.put("userGrowthData", userService.getUserGrowthData());
            return ResponseEntity.ok(stats);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    // ==================== PROBLEM MANAGEMENT ENDPOINTS ====================

   @GetMapping("/problems")
public ResponseEntity<List<Map<String, Object>>> getProblems(
        @RequestParam(required = false) String difficulty,
        @RequestParam(required = false) String category,
        @RequestParam(required = false) String status) {

    try {
        List<Problem> problems = problemService.getProblemsWithFilters(difficulty, category);
        if (problems == null) {
            problems = new ArrayList<>();
        }

        List<Map<String, Object>> problemDTOs = problems.stream().map(problem -> {
            Map<String, Object> problemMap = new HashMap<>();
            problemMap.put("id", problem.getId());
            problemMap.put("title", problem.getTitle() != null ? problem.getTitle() : "Untitled");
            problemMap.put("difficulty", problem.getDifficulty() != null ? problem.getDifficulty().name() : "UNKNOWN");

            // Get categories as list of names instead of single category
            List<String> categoryNames = new ArrayList<>();
            if (problem.getCategories() != null) {
                categoryNames = problem.getCategories().stream()
                        .filter(cat -> cat != null && cat.getName() != null)
                        .map(Category::getName)
                        .collect(Collectors.toList());
            }

            problemMap.put("categories", categoryNames);
            problemMap.put("categoryCount", categoryNames.size());

            // Use "category" field for frontend compatibility (comma-separated string)
            problemMap.put("category", String.join(", ", categoryNames));

            // Add status field
            problemMap.put("status", problem.getStatus() != null ? problem.getStatus().name() : "UNKNOWN");

            problemMap.put("submissionCount", submissionService.getProblemSubmissionCount(problem.getId()));

            // Calculate acceptance rate
            long totalSubmissions = submissionService.getProblemSubmissionCount(problem.getId());
            long acceptedSubmissions = submissionService.getProblemAcceptedSubmissionCount(problem.getId());
            String acceptanceRate = totalSubmissions > 0 ?
                String.format("%.1f%%", (acceptedSubmissions * 100.0) / totalSubmissions) : "0%";
            problemMap.put("acceptanceRate", acceptanceRate);

            return problemMap;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(problemDTOs);
    } catch (Exception e) {
        // Log error without printing stack trace
        System.err.println("Error in getProblems: " + e.getMessage());
        return ResponseEntity.status(500).build();
    }
}

    @GetMapping("/categories/{categoryId}/problems")
    public ResponseEntity<?> getProblemsByCategory(@PathVariable Long categoryId) {
        try {
            List<Problem> problems = problemService.getProblemsByCategory(categoryId);
            if (problems == null) {
                problems = new ArrayList<>();
            }

            List<Map<String, Object>> problemDTOs = problems.stream().map(problem -> {
                Map<String, Object> problemMap = new HashMap<>();
                problemMap.put("id", problem.getId());
                problemMap.put("title", problem.getTitle() != null ? problem.getTitle() : "Untitled");
                problemMap.put("difficulty", problem.getDifficulty() != null ? problem.getDifficulty().name() : "UNKNOWN");

                // Get categories as list of names instead of single category
                List<String> categoryNames = new ArrayList<>();
                if (problem.getCategories() != null) {
                    categoryNames = problem.getCategories().stream()
                            .filter(cat -> cat != null && cat.getName() != null)
                            .map(Category::getName)
                            .collect(Collectors.toList());
                }

                problemMap.put("categories", categoryNames);
                problemMap.put("categoryCount", categoryNames.size());

                // Use "category" field for frontend compatibility (comma-separated string)
                problemMap.put("category", String.join(", ", categoryNames));

                // Add status field
                problemMap.put("status", problem.getStatus() != null ? problem.getStatus().name() : "UNKNOWN");

                problemMap.put("submissionCount", submissionService.getProblemSubmissionCount(problem.getId()));

                // Calculate acceptance rate
                long totalSubmissions = submissionService.getProblemSubmissionCount(problem.getId());
                long acceptedSubmissions = submissionService.getProblemAcceptedSubmissionCount(problem.getId());
                String acceptanceRate = totalSubmissions > 0 ?
                    String.format("%.1f%%", (acceptedSubmissions * 100.0) / totalSubmissions) : "0%";
                problemMap.put("acceptanceRate", acceptanceRate);

                return problemMap;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(problemDTOs);
        } catch (Exception e) {
            // Log error without printing stack trace
            System.err.println("Error in getProblemsByCategory: " + e.getMessage());
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/problems")
    public ResponseEntity<?> createProblem(@RequestBody ProblemRequest problemRequest) {
        try {
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

            // Set additional fields from ProblemRequest
            if (problemRequest.getConstraints() != null) {
                problem.setConstraints(problemRequest.getConstraints());
            }
            if (problemRequest.getPoints() != null) {
                problem.setPoints(problemRequest.getPoints());
            }
            if (problemRequest.getTags() != null) {
                problem.setTags(problemRequest.getTags());
            }
            if (problemRequest.getIsPrivate() != null) {
                problem.setIsPrivate(problemRequest.getIsPrivate());
            }
            if (problemRequest.getFunctionName() != null) {
                problem.setFunctionName(problemRequest.getFunctionName());
            }
            if (problemRequest.getParameters() != null) {
                problem.setParameters(problemRequest.getParameters());
            }
            if (problemRequest.getReturnType() != null) {
                problem.setReturnType(problemRequest.getReturnType());
            }

            // Set categories if provided
            if (problemRequest.getCategoryIds() != null && !problemRequest.getCategoryIds().isEmpty()) {
                Set<Category> categories = new HashSet<>();
                for (Long categoryId : problemRequest.getCategoryIds()) {
                    Category category = categoryService.getCategoryById(categoryId).orElse(null);
                    if (category != null) {
                        categories.add(category);
                    }
                }
                problem.setCategories(categories);
            }

            // Get current authenticated admin user ID as creator
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User creator = userService.findByUsername(username);
            Long creatorId = creator.getId();

            Problem createdProblem = problemService.createProblem(problem, creatorId);

            // Add test cases after problem creation
            if (problemRequest.getTestCases() != null && !problemRequest.getTestCases().isEmpty()) {
                for (TestCaseRequest tcRequest : problemRequest.getTestCases()) {
                    TestCase testCase = new TestCase();
                    testCase.setInputData(tcRequest.getInputData());
                    testCase.setExpectedOutput(tcRequest.getExpectedOutput());
                    testCase.setIsSample(tcRequest.getIsSample() != null ? tcRequest.getIsSample() : false);
                    testCase.setExplanation(tcRequest.getExplanation());
                    testCase.setTestCaseName(tcRequest.getTestCaseName());
                    problemService.addTestCase(createdProblem.getId(), testCase);
                }
            }

            // Add code templates after problem creation
            if (problemRequest.getCodeTemplates() != null && !problemRequest.getCodeTemplates().isEmpty()) {
                for (CodeTemplateRequest ctRequest : problemRequest.getCodeTemplates()) {
                    CodeTemplate codeTemplate = new CodeTemplate();
                    codeTemplate.setLanguage(CodeTemplate.Language.valueOf(ctRequest.getLanguage().toUpperCase()));
                    // Set visible code (what users see and edit)
                    codeTemplate.setVisibleCode(ctRequest.getVisibleCode());
                    // Set hidden code (execution wrapper with {{USER_CODE}} placeholder)
                    codeTemplate.setHiddenCode(ctRequest.getHiddenCode());
                    // For backward compatibility, set templateCode to visibleCode
                    codeTemplate.setTemplateCode(ctRequest.getVisibleCode());
                    problemService.addCodeTemplate(createdProblem.getId(), codeTemplate);
                }
            }
            return ResponseEntity.ok(createdProblem);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create problem: " + e.getMessage());
        }
    }

    @PutMapping("/problems/{problemId}")
    public ResponseEntity<?> updateProblem(@PathVariable Long problemId, @RequestBody ProblemRequest problemRequest) {
        try {
            // Adjust based on your ProblemService method signature
            Problem problem = problemService.updateProblem(problemId, problemRequest);
            return ResponseEntity.ok(problem);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update problem: " + e.getMessage());
        }
    }

    @DeleteMapping("/problems/{problemId}")
    public ResponseEntity<?> deleteProblem(@PathVariable Long problemId) {
        try {
            // Adjust based on your ProblemService method signature
            problemService.deleteProblem(problemId);
            return ResponseEntity.ok(Map.of("message", "Problem deleted successfully"));
        } catch (RuntimeException e) {
            // Handle specific business logic exceptions
            if (e.getMessage() != null) {
                if (e.getMessage().contains("Problem not found")) {
                    return ResponseEntity.notFound().build();
                } else if (e.getMessage().contains("Not authorized")) {
                    return ResponseEntity.status(403).body("Not authorized to delete this problem");
                }
            }
            // Handle database constraint violations or other runtime exceptions
            return ResponseEntity.badRequest().body("Failed to delete problem: " + e.getMessage());
        } catch (Exception e) {
            // Handle any other unexpected exceptions
            System.err.println("Unexpected error deleting problem " + problemId + ": " + e.getMessage());
            return ResponseEntity.status(500).body("An unexpected error occurred while deleting the problem");
        }
    }

    @PutMapping("/problems/{problemId}/status")
    public ResponseEntity<?> updateProblemStatus(@PathVariable Long problemId, @RequestBody Map<String, Boolean> request) {
        try {
            boolean published = request.get("published");
            problemService.updateProblemStatus(problemId, published);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update problem status: " + e.getMessage());
        }
    }

    // ==================== DETAILED PROBLEM MANAGEMENT ENDPOINTS ====================

    @GetMapping("/problems/{problemId}/details")
    public ResponseEntity<ProblemResponseDTO> getProblemDetails(@PathVariable Long problemId) {
        try {
            Optional<Problem> problemOpt = problemService.getProblemById(problemId);
            if (problemOpt.isEmpty()) {
                Map<String, Object> errorResponse = new HashMap<>();
                errorResponse.put("error", "Problem not found");
                errorResponse.put("problemId", problemId);
                return ResponseEntity.status(404).body(null);
            }
            Problem problem = problemOpt.get();

            ProblemResponseDTO dto = new ProblemResponseDTO(problem);
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Internal server error");
            errorResponse.put("message", e.getMessage() != null ? e.getMessage() : "Unknown error");
            errorResponse.put("problemId", problemId);
            return ResponseEntity.status(500).body(null);
        }
    }

    @PostMapping("/problems/{problemId}/test-cases")
    public ResponseEntity<?> addTestCase(@PathVariable Long problemId, @RequestBody TestCaseRequest request) {
        try {
            TestCase testCase = new TestCase();
            testCase.setInputData(request.getInputData());
            testCase.setExpectedOutput(request.getExpectedOutput());
            testCase.setIsSample(request.getIsSample() != null ? request.getIsSample() : false);
            testCase.setExplanation(request.getExplanation());
            testCase.setTestCaseName(request.getTestCaseName());

            problemService.addTestCase(problemId, testCase);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to add test case: " + e.getMessage());
        }
    }

    @PutMapping("/problems/{problemId}/test-cases/{testCaseId}")
    public ResponseEntity<?> updateTestCase(@PathVariable Long problemId, @PathVariable Long testCaseId,
                                           @RequestBody TestCaseRequest request) {
        try {
            TestCase testCase = new TestCase();
            testCase.setId(testCaseId);
            testCase.setInputData(request.getInputData());
            testCase.setExpectedOutput(request.getExpectedOutput());
            testCase.setIsSample(request.getIsSample() != null ? request.getIsSample() : false);
            testCase.setExplanation(request.getExplanation());
            testCase.setTestCaseName(request.getTestCaseName());

            problemService.updateTestCase(problemId, testCaseId, testCase);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update test case: " + e.getMessage());
        }
    }

    @DeleteMapping("/problems/{problemId}/test-cases/{testCaseId}")
    public ResponseEntity<?> deleteTestCase(@PathVariable Long problemId, @PathVariable Long testCaseId) {
        try {
            problemService.deleteTestCase(problemId, testCaseId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete test case: " + e.getMessage());
        }
    }

    @PostMapping("/problems/{problemId}/test-cases/bulk")
    public ResponseEntity<?> bulkUpdateTestCases(@PathVariable Long problemId,
                                                @RequestBody List<TestCaseRequest> requests) {
        try {
            List<TestCase> testCases = requests.stream().map(req -> {
                TestCase tc = new TestCase();
                tc.setInputData(req.getInputData());
                tc.setExpectedOutput(req.getExpectedOutput());
                tc.setIsSample(req.getIsSample() != null ? req.getIsSample() : false);
                tc.setExplanation(req.getExplanation());
                tc.setTestCaseName(req.getTestCaseName());
                return tc;
            }).collect(Collectors.toList());

            problemService.bulkUpdateTestCases(problemId, testCases);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to bulk update test cases: " + e.getMessage());
        }
    }

    @GetMapping("/problems/{problemId}/code-templates")
    public ResponseEntity<List<Map<String, Object>>> getCodeTemplates(@PathVariable Long problemId) {
        try {
            List<CodeTemplate> codeTemplates = problemService.getCodeTemplates(problemId);
            List<Map<String, Object>> templateDTOs = codeTemplates.stream().map(ct -> {
                Map<String, Object> templateMap = new HashMap<>();
                templateMap.put("id", ct.getId());
                templateMap.put("language", ct.getLanguage().name());
                templateMap.put("templateCode", ct.getTemplateCode());
                templateMap.put("visibleCode", ct.getVisibleCode());
                templateMap.put("hiddenCode", ct.getHiddenCode());
                return templateMap;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(templateDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/problems/{problemId}/code-templates")
    public ResponseEntity<?> addCodeTemplate(@PathVariable Long problemId, @RequestBody CodeTemplateRequest request) {
        try {
            CodeTemplate codeTemplate = new CodeTemplate();
            codeTemplate.setLanguage(CodeTemplate.Language.valueOf(request.getLanguage().toUpperCase()));
            codeTemplate.setTemplateCode(request.getVisibleCode());
            codeTemplate.setHiddenCode(request.getHiddenCode());

            problemService.addCodeTemplate(problemId, codeTemplate);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to add code template: " + e.getMessage());
        }
    }

    @PutMapping("/problems/{problemId}/code-templates/{templateId}")
    public ResponseEntity<?> updateCodeTemplate(@PathVariable Long problemId, @PathVariable Long templateId,
                                               @RequestBody CodeTemplateRequest request) {
        try {
            CodeTemplate codeTemplate = new CodeTemplate();
            codeTemplate.setId(templateId);
            codeTemplate.setLanguage(CodeTemplate.Language.valueOf(request.getLanguage().toUpperCase()));
            codeTemplate.setTemplateCode(request.getVisibleCode());
            codeTemplate.setHiddenCode(request.getHiddenCode());

            problemService.updateCodeTemplate(problemId, templateId, codeTemplate);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update code template: " + e.getMessage());
        }
    }

    @DeleteMapping("/problems/{problemId}/code-templates/{templateId}")
    public ResponseEntity<?> deleteCodeTemplate(@PathVariable Long problemId, @PathVariable Long templateId) {
        try {
            problemService.deleteCodeTemplate(problemId, templateId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete code template: " + e.getMessage());
        }
    }

    // ==================== CATEGORY MANAGEMENT ENDPOINTS ====================

    @GetMapping("/categories")
    public ResponseEntity<List<Map<String, Object>>> getCategories() {
        try {
            List<Category> categories = categoryService.getAllCategories();
            List<Map<String, Object>> categoryDTOs = categories.stream().map(category -> {
                Map<String, Object> categoryMap = new HashMap<>();
                categoryMap.put("id", category.getId());
                categoryMap.put("name", category.getName());
                categoryMap.put("description", category.getDescription());
                // Adjust based on your Category entity - no color field, so default
                categoryMap.put("color", "#000000");
                categoryMap.put("problemCount", problemService.getProblemCountByCategory(category.getId()));
                return categoryMap;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(categoryDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/categories")
    public ResponseEntity<?> createCategory(@RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String description = request.get("description");
            String color = request.get("color");

            // Adjust based on your CategoryService method signature
            Category category = categoryService.createCategory(name, description, color);

            Map<String, Object> response = new HashMap<>();
            response.put("id", category.getId());
            response.put("name", category.getName());
            response.put("description", category.getDescription());
            response.put("color", category.getColor() != null ? category.getColor() : "#000000");
            response.put("problemCount", 0);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create category: " + e.getMessage());
        }
    }

    @PutMapping("/categories/{categoryId}")
    public ResponseEntity<?> updateCategory(@PathVariable Long categoryId, @RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String description = request.get("description");
            String color = request.get("color");

            // Adjust based on your CategoryService method signature
            Category category = categoryService.updateCategory(categoryId, name, description, color);
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update category: " + e.getMessage());
        }
    }

    @DeleteMapping("/categories/{categoryId}")
    public ResponseEntity<?> deleteCategory(@PathVariable Long categoryId) {
        try {
            categoryService.deleteCategory(categoryId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete category: " + e.getMessage());
        }
    }

    // ==================== SUBMISSION MANAGEMENT ENDPOINTS ====================

    @GetMapping("/submissions")
    public ResponseEntity<List<Map<String, Object>>> getSubmissions(
            @RequestParam(required = false) String user,
            @RequestParam(required = false) String problem,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String language) {

        try {
            // Adjust based on your SubmissionService method signature
            List<Submission> submissions = submissionService.getSubmissionsWithFilters(user, problem, status, language);
            List<Map<String, Object>> submissionDTOs = submissions.stream().map(submission -> {
                Map<String, Object> submissionMap = new HashMap<>();
                submissionMap.put("id", submission.getId());
                submissionMap.put("user", submission.getUser().getUsername());
                submissionMap.put("problem", submission.getProblem().getTitle());
                submissionMap.put("language", submission.getLanguage());
                submissionMap.put("status", submission.getStatus().name());
                submissionMap.put("executionTime", submission.getExecutionTime() + "ms");
                submissionMap.put("submittedAt", submission.getSubmittedAt().toString());
                submissionMap.put("code", submission.getCode());
                return submissionMap;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(submissionDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/submissions/{submissionId}/rerun")
    public ResponseEntity<?> rerunSubmission(@PathVariable Long submissionId) {
        try {
            submissionService.rerunSubmission(submissionId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to rerun submission: " + e.getMessage());
        }
    }

    @GetMapping("/submissions/{submissionId}/code")
    public ResponseEntity<?> getSubmissionCode(@PathVariable Long submissionId) {
        try {
            String code = submissionService.getSubmissionCode(submissionId);
            Map<String, String> response = new HashMap<>();
            response.put("code", code);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get submission code: " + e.getMessage());
        }
    }

    // ==================== CONTEST MANAGEMENT ENDPOINTS ====================

    @GetMapping("/contests")
    public ResponseEntity<List<Map<String, Object>>> getContests() {
        try {
            List<Contest> contests = contestService.getAllContests();
            List<Map<String, Object>> contestDTOs = contests.stream().map(contest -> {
                Map<String, Object> contestMap = new HashMap<>();
                contestMap.put("id", contest.getId());
                contestMap.put("title", contest.getTitle());
                contestMap.put("description", contest.getDescription());
                contestMap.put("startTime", contest.getStartTime().toString());
                contestMap.put("endTime", contest.getEndTime().toString());
                contestMap.put("status", contest.getStatus().name());
                contestMap.put("participantCount", contestService.getParticipantCount(contest.getId()));
                return contestMap;
            }).collect(Collectors.toList());

            return ResponseEntity.ok(contestDTOs);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    @PostMapping("/contests")
    public ResponseEntity<?> createContest(@RequestBody Contest contest) {
        try {
            // Get current authenticated admin user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User creator = userService.findByUsername(username);

            if (creator == null) {
                return ResponseEntity.badRequest().body("Failed to create contest: Creator not found");
            }

            // Set the creator
            contest.setCreatedBy(creator);

            // Set default values if not provided
            if (contest.getIsPublic() == null) {
                contest.setIsPublic(true);
            }
            if (contest.getStatus() == null) {
                contest.setStatus(Contest.Status.UPCOMING);
            }

            // Calculate duration if not provided
            if (contest.getDuration() == null && contest.getStartTime() != null && contest.getEndTime() != null) {
                long durationMinutes = java.time.Duration.between(contest.getStartTime(), contest.getEndTime()).toMinutes();
                contest.setDuration((int) durationMinutes);
            }

            // Validate required fields
            if (contest.getTitle() == null || contest.getTitle().trim().isEmpty()) {
                return ResponseEntity.badRequest().body("Failed to create contest: Title is required");
            }
            if (contest.getStartTime() == null) {
                return ResponseEntity.badRequest().body("Failed to create contest: Start time is required");
            }
            if (contest.getEndTime() == null) {
                return ResponseEntity.badRequest().body("Failed to create contest: End time is required");
            }
            if (contest.getDuration() == null || contest.getDuration() <= 0) {
                return ResponseEntity.badRequest().body("Failed to create contest: Valid duration is required");
            }

            Contest createdContest = contestService.createContest(contest);
            return ResponseEntity.ok(createdContest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create contest: " + e.getMessage());
        }
    }

    @PutMapping("/contests/{contestId}")
    public ResponseEntity<?> updateContest(@PathVariable Long contestId, @RequestBody Contest contest) {
        try {
            Contest updatedContest = contestService.updateContest(contestId, contest);
            return ResponseEntity.ok(updatedContest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to update contest: " + e.getMessage());
        }
    }

    @DeleteMapping("/contests/{contestId}")
    public ResponseEntity<?> deleteContest(@PathVariable Long contestId) {
        try {
            contestService.deleteContest(contestId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete contest: " + e.getMessage());
        }
    }

    // ==================== UTILITY METHODS ====================



    private String formatTimeAgo(java.time.LocalDateTime dateTime) {
        // Simple time ago formatter implementation
        java.time.Duration duration = java.time.Duration.between(dateTime, java.time.LocalDateTime.now());

        if (duration.toMinutes() < 1) {
            return "Just now";
        } else if (duration.toHours() < 1) {
            return duration.toMinutes() + " minutes ago";
        } else if (duration.toDays() < 1) {
            return duration.toHours() + " hours ago";
        } else {
            return duration.toDays() + " days ago";
        }
    }

    // Add this to your AdminController temporarily
    // Replace your debug endpoint with this:
    @GetMapping("/debug")
    public ResponseEntity<?> debugAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        Map<String, Object> debugInfo = new HashMap<>();
        if (authentication != null && authentication.isAuthenticated()) {
            debugInfo.put("username", authentication.getName());
            debugInfo.put("authorities", authentication.getAuthorities().toString());
            debugInfo.put("authenticated", authentication.isAuthenticated());
            debugInfo.put("principal", authentication.getPrincipal().getClass().getSimpleName());
        } else {
            debugInfo.put("error", "No authentication found");
            debugInfo.put("authentication", "null");
        }
        return ResponseEntity.ok(debugInfo);
    }
}
