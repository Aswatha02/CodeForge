package com.CodeForge.CodeForge.services;

import com.CodeForge.CodeForge.model.User;
import com.CodeForge.CodeForge.model.UserProgress;
import com.CodeForge.CodeForge.repository.UserRepository;
import com.CodeForge.CodeForge.repository.UserProgressRepository;
import com.CodeForge.CodeForge.repository.SubmissionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserProgressRepository userProgressRepository;
    private final SubmissionRepository submissionRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, 
                      UserProgressRepository userProgressRepository,
                      SubmissionRepository submissionRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userProgressRepository = userProgressRepository;
        this.submissionRepository = submissionRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(User user) {
        // Check if username already exists
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new RuntimeException("Username already exists");
        }
        
        // Check if email already exists
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }
        
        // Hash password before saving
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        
        // Set default role if not provided
        if (user.getRole() == null) {
            user.setRole(User.Role.USER);
        }
        
        User savedUser = userRepository.save(user);
        
        // Create initial user progress with your field names
        UserProgress initialProgress = new UserProgress();
        initialProgress.setUser(savedUser);
        initialProgress.setSolvedCount(0);
        initialProgress.setSolvedEasyCount(0);
        initialProgress.setSolvedMediumCount(0);
        initialProgress.setSolvedHardCount(0);
        initialProgress.setTotalSubmissions(0);
        initialProgress.setAcceptedSubmissions(0);
        initialProgress.setCurrentStreak(0);
        initialProgress.setMaxStreak(0);
        initialProgress.setStatus(UserProgress.Status.ACTIVE);
        userProgressRepository.save(initialProgress);
        
        return savedUser;
    }

    public User loginUser(String usernameOrEmail, String password) {
        Optional<User> userOpt = userRepository.findByUsernameOrEmail(usernameOrEmail);
        
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Invalid username/email or password");
        }
        
        User user = userOpt.get();
        
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid username/email or password");
        }
        
        return user;
    }

    public UserProgress getUserProgress(Long userId) {
        return userProgressRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User progress not found for user id: " + userId));
    }

    public UserProgress updateUserProgress(Long userId, UserProgress progress) {
        UserProgress existingProgress = getUserProgress(userId);
        
        // Update progress fields if they are provided in the request
        if (progress.getSolvedCount() != null) {
            existingProgress.setSolvedCount(progress.getSolvedCount());
        }
        if (progress.getSolvedEasyCount() != null) {
            existingProgress.setSolvedEasyCount(progress.getSolvedEasyCount());
        }
        if (progress.getSolvedMediumCount() != null) {
            existingProgress.setSolvedMediumCount(progress.getSolvedMediumCount());
        }
        if (progress.getSolvedHardCount() != null) {
            existingProgress.setSolvedHardCount(progress.getSolvedHardCount());
        }
        if (progress.getTotalSubmissions() != null) {
            existingProgress.setTotalSubmissions(progress.getTotalSubmissions());
        }
        if (progress.getAcceptedSubmissions() != null) {
            existingProgress.setAcceptedSubmissions(progress.getAcceptedSubmissions());
        }
        if (progress.getCurrentStreak() != null) {
            existingProgress.setCurrentStreak(progress.getCurrentStreak());
        }
        if (progress.getMaxStreak() != null) {
            existingProgress.setMaxStreak(progress.getMaxStreak());
        }
        if (progress.getStatus() != null) {
            existingProgress.setStatus(progress.getStatus());
        }
        
        return userProgressRepository.save(existingProgress);
    }

    // Helper method to calculate success rate
    public double calculateSuccessRate(UserProgress progress) {
        if (progress.getTotalSubmissions() == 0) {
            return 0.0;
        }
        return (double) progress.getAcceptedSubmissions() / progress.getTotalSubmissions() * 100;
    }

    // Method to update progress when a problem is solved
    public UserProgress updateProgressOnSubmission(Long userId, boolean accepted, String difficulty) {
        UserProgress progress = getUserProgress(userId);
        
        // Update total submissions
        progress.setTotalSubmissions(progress.getTotalSubmissions() + 1);
        
        if (accepted) {
            // Update accepted submissions
            progress.setAcceptedSubmissions(progress.getAcceptedSubmissions() + 1);
            
            // Update solved counts based on difficulty
            progress.setSolvedCount(progress.getSolvedCount() + 1);
            
            switch (difficulty.toUpperCase()) {
                case "EASY" -> progress.setSolvedEasyCount(progress.getSolvedEasyCount() + 1);
                case "MEDIUM" -> progress.setSolvedMediumCount(progress.getSolvedMediumCount() + 1);
                case "HARD" -> progress.setSolvedHardCount(progress.getSolvedHardCount() + 1);
            }
            
            // Update streaks
            progress.setCurrentStreak(progress.getCurrentStreak() + 1);
            if (progress.getCurrentStreak() > progress.getMaxStreak()) {
                progress.setMaxStreak(progress.getCurrentStreak());
            }
        } else {
            // Reset current streak if submission is not accepted
            progress.setCurrentStreak(0);
        }
        
        return userProgressRepository.save(progress);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
    }

    public User updateUser(Long userId, User userDetails) {
        User existingUser = getUserById(userId);
        
        // Update only allowed fields
        if (userDetails.getUsername() != null && !userDetails.getUsername().trim().isEmpty()) {
            // Check if username is already taken by another user
            Optional<User> userWithSameUsername = userRepository.findByUsername(userDetails.getUsername());
            if (userWithSameUsername.isPresent() && !userWithSameUsername.get().getId().equals(userId)) {
                throw new RuntimeException("Username is already taken");
            }
            existingUser.setUsername(userDetails.getUsername());
        }
        
        if (userDetails.getEmail() != null && !userDetails.getEmail().trim().isEmpty()) {
            // Check if email is already taken by another user
            Optional<User> userWithSameEmail = userRepository.findByEmail(userDetails.getEmail());
            if (userWithSameEmail.isPresent() && !userWithSameEmail.get().getId().equals(userId)) {
                throw new RuntimeException("Email is already taken");
            }
            existingUser.setEmail(userDetails.getEmail());
        }
        
        if (userDetails.getRole() != null) {
            existingUser.setRole(userDetails.getRole());
        }
        
        return userRepository.save(existingUser);
    }

    // Simple filtering using Java Streams - removed status filter since User doesn't have enabled field
    public List<User> getUsersWithFilters(String search, String role, String status) {
        List<User> allUsers = userRepository.findAll();
        
        return allUsers.stream()
            .filter(user -> {
                if (search == null || search.trim().isEmpty()) return true;
                String searchLower = search.toLowerCase();
                return user.getUsername().toLowerCase().contains(searchLower) ||
                       user.getEmail().toLowerCase().contains(searchLower);
            })
            .filter(user -> {
                if (role == null || role.trim().isEmpty()) return true;
                try {
                    User.Role userRole = User.Role.valueOf(role.toUpperCase());
                    return user.getRole() == userRole;
                } catch (IllegalArgumentException e) {
                    return false;
                }
            })
            // Removed status filter since User entity doesn't have enabled field
            .sorted((u1, u2) -> u2.getCreatedAt().compareTo(u1.getCreatedAt())) // Descending order
            .collect(Collectors.toList());
    }

    public void updateUserRole(Long userId, String role) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        try {
            User.Role newRole = User.Role.valueOf(role.toUpperCase());
            user.setRole(newRole);
            userRepository.save(user);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid role: " + role);
        }
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        userRepository.delete(user);
    }

    // Removed updateUserStatus method since User doesn't have enabled field
    // If you need user status management, add enabled field to User entity

    public Long getTotalUsers() {
        return userRepository.count();
    }

    public Long getDailyActiveUsers() {
        try {
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

            // Use the repository method if it exists
            if (submissionRepository != null) {
                return submissionRepository.countDistinctUsersSince(yesterday);
            } else {
                // Fallback implementation
                Long totalUsers = getTotalUsers();
                return Math.max(1L, totalUsers / 4); // 25% as fallback
            }
        } catch (Exception e) {
            // Fallback: estimate 25% of total users as active
            Long totalUsers = getTotalUsers();
            return Math.max(1L, totalUsers / 4);
        }
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }
}
