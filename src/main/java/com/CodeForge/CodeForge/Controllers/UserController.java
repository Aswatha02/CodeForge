package com.CodeForge.CodeForge.Controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.CodeForge.CodeForge.model.Submission;
import com.CodeForge.CodeForge.model.User;
import com.CodeForge.CodeForge.model.UserProgress;
import com.CodeForge.CodeForge.services.SubmissionService;
import com.CodeForge.CodeForge.services.UserProgressBackfillService;
import com.CodeForge.CodeForge.services.UserService;
import com.CodeForge.CodeForge.util.JwtUtil;

@RestController
@RequestMapping("/api/users")

public class UserController {

    private final UserService userService;
    private final SubmissionService submissionService;
    private final UserProgressBackfillService backfillService;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    // Constructor injection
    @Autowired
    public UserController(UserService userService, SubmissionService submissionService, 
                         UserProgressBackfillService backfillService, JwtUtil jwtUtil, 
                         @Qualifier("customUserDetailsService") UserDetailsService userDetailsService) {
        this.userService = userService;
        this.submissionService = submissionService;
        this.backfillService = backfillService;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody Map<String, String> registerRequest) {
        try {
            String username = registerRequest.get("username");
            String email = registerRequest.get("email");
            String password = registerRequest.get("password");

            if (username == null || username.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username is required"));
            }
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Email is required"));
            }
            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Password is required"));
            }

            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setRawPassword(password); // This will be hashed in the service

            User registeredUser = userService.registerUser(user);
            return ResponseEntity.ok(registeredUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginRequest) {
        try {
            String usernameOrEmail = loginRequest.get("usernameOrEmail");
            String password = loginRequest.get("password");

            if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Username or email is required"));
            }

            if (password == null || password.trim().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "Password is required"));
            }

            User user = userService.loginUser(usernameOrEmail, password);

            // Generate JWT token
            UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
            String token = jwtUtil.generateToken(userDetails);

            // Return user data with token
            Map<String, Object> response = new HashMap<>();
            response.put("user", user);
            response.put("token", token);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/login")
    public ResponseEntity<?> loginGet() {
        return ResponseEntity.status(405).body(Map.of("error", "Method not allowed. Use POST to login."));
    }

    @GetMapping("/{userId:\\d+}/progress")
    public ResponseEntity<?> getUserProgress(@PathVariable Long userId) {
        try {
            UserProgress progress = userService.getUserProgress(userId);
            return ResponseEntity.ok(progress);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{userId:\\d+}/progress")
    public ResponseEntity<?> updateUserProgress(
            @PathVariable Long userId,
            @RequestBody UserProgress progress) {
        try {
            UserProgress updatedProgress = userService.updateUserProgress(userId, progress);
            return ResponseEntity.ok(updatedProgress);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Admin endpoints
    @GetMapping("/admin/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{userId:\\d+}")
    public ResponseEntity<?> getUserById(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{userId:\\d+}")
    public ResponseEntity<?> updateUser(@PathVariable Long userId, @RequestBody User user) {
        try {
            User updatedUser = userService.updateUser(userId, user);
            return ResponseEntity.ok(updatedUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{userId:\\d+}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        try {
            userService.deleteUser(userId);
            return ResponseEntity.ok(Map.of("message", "User deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{userId:\\d+}/profile")
    public ResponseEntity<?> getUserProfile(@PathVariable Long userId) {
        try {
            User user = userService.getUserById(userId);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{userId:\\d+}/ban")
    public ResponseEntity<?> banUser(@PathVariable Long userId) {
        try {
            userService.banUser(userId);
            return ResponseEntity.ok(Map.of("message", "User banned successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{userId:\\d+}/unban")
    public ResponseEntity<?> unbanUser(@PathVariable Long userId) {
        try {
            userService.unbanUser(userId);
            return ResponseEntity.ok(Map.of("message", "User unbanned successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{userId:\\d+}/reset-progress")
    public ResponseEntity<?> resetUserProgress(@PathVariable Long userId) {
        try {
            userService.resetUserProgress(userId);
            return ResponseEntity.ok(Map.of("message", "User progress reset successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me/submissions")
    public ResponseEntity<?> getCurrentUserSubmissions(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String language,
            @RequestParam(required = false) String problemId) {
        try {
            // Get current user from security context
            org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUsername(username);

            // Get all submissions for the user
            List<Submission> submissions = submissionService.getRecentSubmissionsByUser(user.getId(), 100); // Get last 100
            
            // Apply filters
            if (status != null && !status.isEmpty()) {
                submissions = submissions.stream()
                    .filter(s -> s.getStatus().name().equalsIgnoreCase(status))
                    .collect(java.util.stream.Collectors.toList());
            }
            if (language != null && !language.isEmpty()) {
                submissions = submissions.stream()
                    .filter(s -> s.getLanguage().name().equalsIgnoreCase(language))
                    .collect(java.util.stream.Collectors.toList());
            }
            if (problemId != null && !problemId.isEmpty()) {
                submissions = submissions.stream()
                    .filter(s -> s.getProblem().getId().toString().equals(problemId) || 
                                s.getProblem().getTitle().toLowerCase().contains(problemId.toLowerCase()))
                    .collect(java.util.stream.Collectors.toList());
            }

            // Convert to response format
            List<Map<String, Object>> submissionList = new ArrayList<>();
            for (Submission submission : submissions) {
                Map<String, Object> submissionData = new HashMap<>();
                submissionData.put("id", submission.getId());
                submissionData.put("problemId", submission.getProblem().getId());
                submissionData.put("problem", Map.of("title", submission.getProblem().getTitle()));
                submissionData.put("status", submission.getStatus());
                submissionData.put("language", submission.getLanguage());
                submissionData.put("executionTime", submission.getExecutionTime());
                submissionData.put("memoryUsed", submission.getMemoryUsed());
                submissionData.put("submittedAt", submission.getSubmittedAt());
                submissionList.add(submissionData);
            }

            return ResponseEntity.ok(submissionList);
        } catch (Exception e) {
            System.out.println("Error fetching user submissions: " + e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/me/stats")
    public ResponseEntity<?> getCurrentUserStats() {
        try {
            // Get current user from security context
            org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            String username = authentication.getName();
            User user = userService.findByUsername(username);

            Map<String, Object> stats = new HashMap<>();
            stats.put("problemsSolved", submissionService.getUserAcceptedSubmissionCount(user.getId()));
            stats.put("totalSubmissions", submissionService.getUserSubmissionCount(user.getId()));
            stats.put("ranking", userService.getUserRank(user.getId()));
            
            // Calculate current streak
            int currentStreak = calculateCurrentStreak(user.getId());
            stats.put("currentStreak", currentStreak);

            // Calculate accuracy
            long total = (Long) stats.get("totalSubmissions");
            long solved = (Long) stats.get("problemsSolved");
            double accuracy = total > 0 ? (solved * 100.0) / total : 0.0;
            stats.put("accuracy", Math.round(accuracy * 100.0) / 100.0);

            // Difficulty breakdown (mock for now, can be implemented later)
            stats.put("easy", 0);
            stats.put("medium", 0);
            stats.put("hard", 0);

            // Get recent activity (last 5 submissions)
            List<Submission> recentSubmissions = submissionService.getRecentSubmissionsByUser(user.getId(), 5);
            List<Map<String, Object>> recentActivity = new ArrayList<>();
            for (Submission submission : recentSubmissions) {
                Map<String, Object> activity = new HashMap<>();
                activity.put("description", "Submitted solution for " + submission.getProblem().getTitle());
                activity.put("timestamp", submission.getSubmittedAt());
                activity.put("status", submission.getStatus());
                recentActivity.add(activity);
            }
            stats.put("recentActivity", recentActivity);

            // Get solved problems (problems with accepted submissions)
            List<Submission> acceptedSubmissions = submissionService.getUserAcceptedSubmissions(user.getId());
            System.out.println("DEBUG: Found " + acceptedSubmissions.size() + " accepted submissions");
            
            List<Map<String, Object>> solvedProblems = new ArrayList<>();
            // Use a set to track unique problems
            java.util.Set<Long> seenProblemIds = new java.util.HashSet<>();
            
            for (Submission submission : acceptedSubmissions) {
                if (!seenProblemIds.contains(submission.getProblem().getId())) {
                    seenProblemIds.add(submission.getProblem().getId());
                    try {
                        Map<String, Object> problemInfo = new HashMap<>();
                        problemInfo.put("title", submission.getProblem().getTitle());
                        problemInfo.put("difficulty", submission.getProblem().getDifficulty());
                        
                        // Get first category name or default to "Uncategorized"
                        String categoryName = "Uncategorized";
                        try {
                            if (submission.getProblem().getCategories() != null && !submission.getProblem().getCategories().isEmpty()) {
                                categoryName = submission.getProblem().getCategories().iterator().next().getName();
                            }
                        } catch (Exception e) {
                            System.out.println("DEBUG: Could not fetch category for problem " + submission.getProblem().getId() + ": " + e.getMessage());
                            // Keep default "Uncategorized"
                        }
                        problemInfo.put("category", categoryName);
                        problemInfo.put("solvedAt", submission.getSubmittedAt().toLocalDate().toString());
                        solvedProblems.add(problemInfo);
                        System.out.println("DEBUG: Added solved problem: " + submission.getProblem().getTitle());
                    } catch (Exception e) {
                        System.out.println("DEBUG: Error processing problem " + submission.getProblem().getId() + ": " + e.getMessage());
                    }
                }
            }
            System.out.println("DEBUG: Total unique solved problems: " + solvedProblems.size());
            stats.put("solvedProblems", solvedProblems);

            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Calculate the current streak of consecutive days with accepted submissions
     */
    private int calculateCurrentStreak(Long userId) {
        try {
            List<Submission> acceptedSubmissions = submissionService.getUserAcceptedSubmissions(userId);
            
            if (acceptedSubmissions.isEmpty()) {
                return 0;
            }

            // Get unique dates of accepted submissions (sorted descending)
            java.util.Set<java.time.LocalDate> submissionDates = new java.util.TreeSet<>(java.util.Collections.reverseOrder());
            for (Submission submission : acceptedSubmissions) {
                submissionDates.add(submission.getSubmittedAt().toLocalDate());
            }

            java.time.LocalDate today = java.time.LocalDate.now();
            java.time.LocalDate yesterday = today.minusDays(1);
            
            // Check if user has submitted today or yesterday (to keep streak alive)
            java.time.LocalDate mostRecentDate = submissionDates.iterator().next();
            if (!mostRecentDate.equals(today) && !mostRecentDate.equals(yesterday)) {
                return 0; // Streak is broken
            }

            // Count consecutive days
            int streak = 0;
            java.time.LocalDate expectedDate = mostRecentDate;
            
            for (java.time.LocalDate date : submissionDates) {
                if (date.equals(expectedDate)) {
                    streak++;
                    expectedDate = expectedDate.minusDays(1);
                } else {
                    break; // Streak broken
                }
            }

            return streak;
        } catch (Exception e) {
            System.out.println("Error calculating streak: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Admin endpoint to backfill UserProgress from existing submissions
     * This is a one-time migration endpoint
     */
    @PostMapping("/admin/backfill-progress")
    public ResponseEntity<?> backfillUserProgress() {
        try {
            // TODO: Add admin authorization check here
            Map<String, Object> result = backfillService.backfillUserProgress();
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Backfill failed: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }

    /**
     * Get backfill status
     */
    @GetMapping("/admin/backfill-status")
    public ResponseEntity<?> getBackfillStatus() {
        try {
            Map<String, Object> status = backfillService.getBackfillStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("error", "Failed to get status: " + e.getMessage());
            return ResponseEntity.status(500).body(error);
        }
    }
}
