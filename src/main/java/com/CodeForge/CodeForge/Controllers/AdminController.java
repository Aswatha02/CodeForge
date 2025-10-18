package com.CodeForge.CodeForge.Controllers;

import com.CodeForge.CodeForge.model.*;
import com.CodeForge.CodeForge.services.*;
import com.CodeForge.CodeForge.dto.*;
import com.CodeForge.CodeForge.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.stream.Collectors;

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

    // ==================== USER MANAGEMENT ENDPOINTS ====================

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status) {

        try {
            List<User> users = userService.getUsersWithFilters(search, role, status);
            List<Map<String, Object>> userDTOs = users.stream().map(user -> {
                Map<String, Object> userMap = new HashMap<>();
                userMap.put("id", user.getId());
                userMap.put("username", user.getUsername());
                userMap.put("email", user.getEmail());
                userMap.put("role", user.getRole().name());
                // Assuming User has an 'enabled' field - adjust based on your User entity
                userMap.put("joinDate", user.getCreatedAt() != null ? user.getCreatedAt().toString() : "N/A");
                userMap.put("submissionCount", submissionService.getUserSubmissionCount(user.getId()));
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
        e.printStackTrace(); // Add logging for debugging
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

            // Set test cases if provided
            if (problemRequest.getTestCases() != null && !problemRequest.getTestCases().isEmpty()) {
                List<TestCase> testCases = new ArrayList<>();
                for (TestCaseRequest tcRequest : problemRequest.getTestCases()) {
                    TestCase testCase = new TestCase();
                    testCase.setInputData(tcRequest.getInputData());
                    testCase.setExpectedOutput(tcRequest.getExpectedOutput());
                    testCase.setIsSample(tcRequest.getIsSample() != null ? tcRequest.getIsSample() : false);
                    testCases.add(testCase);
                }
                problem.setTestCases(testCases);
            }

            // Set code templates if provided
            if (problemRequest.getCodeTemplates() != null && !problemRequest.getCodeTemplates().isEmpty()) {
                List<CodeTemplate> codeTemplates = new ArrayList<>();
                for (CodeTemplateRequest ctRequest : problemRequest.getCodeTemplates()) {
                    CodeTemplate codeTemplate = new CodeTemplate();
                    codeTemplate.setLanguage(CodeTemplate.Language.valueOf(ctRequest.getLanguage().toUpperCase()));
                    codeTemplate.setTemplate(ctRequest.getTemplate());
                    codeTemplates.add(codeTemplate);
                }
                problem.setCodeTemplates(codeTemplates);
            }

            // Get current authenticated admin user ID as creator
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User creator = userService.findByUsername(username);
            Long creatorId = creator.getId();

            Problem createdProblem = problemService.createProblem(problem, creatorId);
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
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to delete problem: " + e.getMessage());
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
            Contest createdContest = contestService.createContest(contest);
            return ResponseEntity.ok(createdContest);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to create contest: " + e.getMessage());
        }
    }

    // ==================== UTILITY METHODS ====================

    private String getCategoryName(Problem problem) {
        // Problem has categories (Set<Category>), not a single category
        if (problem.getCategories() != null && !problem.getCategories().isEmpty()) {
            return problem.getCategories().iterator().next().getName();
        }
        return "Uncategorized";
    }

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
