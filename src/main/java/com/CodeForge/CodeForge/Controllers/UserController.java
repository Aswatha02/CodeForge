package com.CodeForge.CodeForge.Controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.CodeForge.CodeForge.model.User;
import com.CodeForge.CodeForge.model.UserProgress;
import com.CodeForge.CodeForge.services.SubmissionService;
import com.CodeForge.CodeForge.services.UserService;
import com.CodeForge.CodeForge.util.JwtUtil;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})
public class UserController {

    private final UserService userService;
    private final SubmissionService submissionService;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    // Constructor injection
    @Autowired
    public UserController(UserService userService, SubmissionService submissionService, JwtUtil jwtUtil, UserDetailsService userDetailsService) {
        this.userService = userService;
        this.submissionService = submissionService;
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
            stats.put("rank", userService.getUserRank(user.getId()));

            // Calculate accuracy
            long total = (Long) stats.get("totalSubmissions");
            long solved = (Long) stats.get("problemsSolved");
            double accuracy = total > 0 ? (solved * 100.0) / total : 0.0;
            stats.put("accuracy", Math.round(accuracy * 100.0) / 100.0);

            // Difficulty breakdown (mock for now, can be implemented later)
            stats.put("easy", 0);
            stats.put("medium", 0);
            stats.put("hard", 0);

            return ResponseEntity.ok(stats);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
