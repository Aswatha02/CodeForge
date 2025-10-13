package com.CodeForge.CodeForge.Controllers;

import com.CodeForge.CodeForge.model.User;
import com.CodeForge.CodeForge.model.UserProgress;
import com.CodeForge.CodeForge.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class UserController {
    
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User user) {
        try {
            User registeredUser = userService.registerUser(user);
            return ResponseEntity.ok(registeredUser);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/login")
public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginRequest) {
    try {
        String usernameOrEmail = loginRequest.get("usernameOrEmail");
        String password = loginRequest.get("password");
        
        User user = userService.loginUser(usernameOrEmail, password);
        return ResponseEntity.ok(user);
    } catch (RuntimeException e) {
        return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
    }
}

    @GetMapping("/{userId}/progress")
    public ResponseEntity<?> getUserProgress(@PathVariable Long userId) {
        try {
            UserProgress progress = userService.getUserProgress(userId);
            return ResponseEntity.ok(progress);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{userId}/progress")
    public ResponseEntity<?> updateUserProgress(
            @PathVariable Long userId,
            @RequestBody UserProgress progress) {
        try {
            UserProgress updatedProgress = userService.updateUserProgress(userId, progress);
            return ResponseEntity.ok(updatedProgress);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    // Admin endpoints
    @GetMapping("/admin/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }
}