package com.CodeForge.CodeForge.services;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.CodeForge.CodeForge.model.User;
import com.CodeForge.CodeForge.model.UserProgress;
import com.CodeForge.CodeForge.repository.SubmissionRepository;
import com.CodeForge.CodeForge.repository.UserProgressRepository;
import com.CodeForge.CodeForge.repository.UserRepository;

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

        if (user.getRawPassword() == null) {
        throw new IllegalArgumentException("rawPassword cannot be null");
        }
        
        // Hash password before saving
        user.setPasswordHash(passwordEncoder.encode(user.getRawPassword()));
        
        // Set default role if not provided
        if (user.getRole() == null) {
            // Special case for admin user
            if ("admin@codeforge.com".equals(user.getEmail())) {
                user.setRole(User.Role.ADMIN);
            } else {
                user.setRole(User.Role.USER);
            }
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

        // Update last login timestamp
        user.setLastLogin(LocalDateTime.now());
        userRepository.save(user);

        return user;
    }

    public UserProgress getUserProgress(Long userId) {
        Optional<UserProgress> progress = userProgressRepository.findByUserId(userId);
        if (progress.isPresent()) {
            return progress.get();
        } else {
            // Create initial progress for existing users who don't have it
            User user = getUserById(userId);
            UserProgress initialProgress = new UserProgress();
            initialProgress.setUser(user);
            initialProgress.setSolvedCount(0);
            initialProgress.setSolvedEasyCount(0);
            initialProgress.setSolvedMediumCount(0);
            initialProgress.setSolvedHardCount(0);
            initialProgress.setTotalSubmissions(0);
            initialProgress.setAcceptedSubmissions(0);
            initialProgress.setCurrentStreak(0);
            initialProgress.setMaxStreak(0);
            initialProgress.setStatus(UserProgress.Status.ACTIVE);
            return userProgressRepository.save(initialProgress);
        }
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

    // Enhanced filtering using Java Streams with status support
    public List<User> getUsersWithFilters(String search, String role, String status, String sortBy, String sortOrder, int page, int size) {
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
            .filter(user -> {
                if (status == null || status.trim().isEmpty()) return true;
                try {
                    User.Status userStatus = User.Status.valueOf(status.toUpperCase());
                    return user.getStatus() == userStatus;
                } catch (IllegalArgumentException e) {
                    return false;
                }
            })
            .sorted((u1, u2) -> {
                int comparison = 0;
                if (sortBy != null) {
                    switch (sortBy.toLowerCase()) {
                        case "username":
                            comparison = u1.getUsername().compareTo(u2.getUsername());
                            break;
                        case "email":
                            comparison = u1.getEmail().compareTo(u2.getEmail());
                            break;
                        case "role":
                            comparison = u1.getRole().compareTo(u2.getRole());
                            break;
                        case "status":
                            comparison = u1.getStatus().compareTo(u2.getStatus());
                            break;
                        case "createdat":
                        case "joindate":
                            comparison = u1.getCreatedAt().compareTo(u2.getCreatedAt());
                            break;
                        case "lastlogin":
                            LocalDateTime l1 = u1.getLastLogin() != null ? u1.getLastLogin() : LocalDateTime.MIN;
                            LocalDateTime l2 = u2.getLastLogin() != null ? u2.getLastLogin() : LocalDateTime.MIN;
                            comparison = l1.compareTo(l2);
                            break;
                        default:
                            comparison = u2.getCreatedAt().compareTo(u1.getCreatedAt());
                    }
                } else {
                    comparison = u2.getCreatedAt().compareTo(u1.getCreatedAt());
                }

                return "desc".equalsIgnoreCase(sortOrder) ? -comparison : comparison;
            })
            .skip((long) page * size)
            .limit(size)
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

    // New user status management methods
    public void banUser(Long userId) {
        User user = getUserById(userId);
        user.setStatus(User.Status.BANNED);
        userRepository.save(user);
    }

    public void unbanUser(Long userId) {
        User user = getUserById(userId);
        user.setStatus(User.Status.ACTIVE);
        userRepository.save(user);
    }

    public void activateUser(Long userId, boolean activate) {
        User user = getUserById(userId);
        user.setStatus(activate ? User.Status.ACTIVE : User.Status.BANNED);
        userRepository.save(user);
    }

    public void resetUserProgress(Long userId) {
        Optional<UserProgress> existing = userProgressRepository.findByUserId(userId);
        if (existing.isPresent()) {
            UserProgress progress = existing.get();
            progress.getSolvedProblems().clear(); // Clear solved problems associations
            progress.setSolvedCount(0);
            progress.setSolvedEasyCount(0);
            progress.setSolvedMediumCount(0);
            progress.setSolvedHardCount(0);
            progress.setTotalSubmissions(0);
            progress.setAcceptedSubmissions(0);
            progress.setCurrentStreak(0);
            progress.setMaxStreak(0);
            progress.setLastActivityDate(null);
            progress.setStatus(UserProgress.Status.ACTIVE);
            progress.setUpdatedAt(LocalDateTime.now());
            userProgressRepository.save(progress);
        } else {
            // Create new progress if none exists (shouldn't happen normally)
            User user = getUserById(userId);
            UserProgress progress = new UserProgress();
            progress.setUser(user);
            progress.setSolvedProblems(new HashSet<>());
            progress.setSolvedCount(0);
            progress.setSolvedEasyCount(0);
            progress.setSolvedMediumCount(0);
            progress.setSolvedHardCount(0);
            progress.setTotalSubmissions(0);
            progress.setAcceptedSubmissions(0);
            progress.setCurrentStreak(0);
            progress.setMaxStreak(0);
            progress.setLastActivityDate(null);
            progress.setStatus(UserProgress.Status.ACTIVE);
            userProgressRepository.save(progress);
        }
    }

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

    public User findById(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public void updateUserStatus(Long userId, String status) {
        User user = getUserById(userId);
        try {
            User.Status userStatus = User.Status.valueOf(status.toUpperCase());
            user.setStatus(userStatus);
            userRepository.save(user);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid status: " + status);
        }
    }
    

    public void updateUserDetails(Long userId, String email, String role, String password) {
        User user = getUserById(userId);

        if (email != null && !email.trim().isEmpty()) {
            // Check if email is already taken by another user
            Optional<User> userWithSameEmail = userRepository.findByEmail(email);
            if (userWithSameEmail.isPresent() && !userWithSameEmail.get().getId().equals(userId)) {
                throw new RuntimeException("Email is already taken");
            }
            user.setEmail(email);
        }

        if (role != null && !role.trim().isEmpty()) {
            try {
                User.Role newRole = User.Role.valueOf(role.toUpperCase());
                user.setRole(newRole);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid role: " + role);
            }
        }

        if (password != null && !password.trim().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(password));
        }

        userRepository.save(user);
    }

    public void updateUserDetails(Long userId, String email, String role, String password, String status) {
        User user = getUserById(userId);

        if (email != null && !email.trim().isEmpty()) {
            // Check if email is already taken by another user
            Optional<User> userWithSameEmail = userRepository.findByEmail(email);
            if (userWithSameEmail.isPresent() && !userWithSameEmail.get().getId().equals(userId)) {
                throw new RuntimeException("Email is already taken");
            }
            user.setEmail(email);
        }

        if (role != null && !role.trim().isEmpty()) {
            try {
                User.Role newRole = User.Role.valueOf(role.toUpperCase());
                user.setRole(newRole);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid role: " + role);
            }
        }

        if (status != null && !status.trim().isEmpty()) {
            try {
                User.Status userStatus = User.Status.valueOf(status.toUpperCase());
                user.setStatus(userStatus);
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Invalid status: " + status);
            }
        }

        if (password != null && !password.trim().isEmpty()) {
            user.setPasswordHash(passwordEncoder.encode(password));
        }

        userRepository.save(user);
    }

    public Long getActiveUsersCount() {
        return userRepository.findAll().stream()
                .filter(user -> user.getStatus() == User.Status.ACTIVE)
                .count();
    }

    public Long getBannedUsersCount() {
        return userRepository.findAll().stream()
                .filter(user -> user.getStatus() == User.Status.BANNED)
                .count();
    }

    public Long getNewUsersThisWeek() {
        LocalDateTime weekAgo = LocalDateTime.now().minusWeeks(1);
        return userRepository.findAll().stream()
                .filter(user -> user.getCreatedAt().isAfter(weekAgo))
                .count();
    }

    public Long getNewUsersThisMonth() {
        LocalDateTime monthAgo = LocalDateTime.now().minusMonths(1);
        return userRepository.findAll().stream()
                .filter(user -> user.getCreatedAt().isAfter(monthAgo))
                .count();
    }

    public List<Map<String, Object>> getUserGrowthData() {
        // Simple implementation - in real app, you'd query database with date aggregations
        List<Map<String, Object>> growthData = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (int i = 29; i >= 0; i--) {
            LocalDateTime date = now.minusDays(i);
            LocalDateTime nextDay = date.plusDays(1);

            long count = userRepository.findAll().stream()
                    .filter(user -> user.getCreatedAt().isAfter(date) && user.getCreatedAt().isBefore(nextDay))
                    .count();

            Map<String, Object> dataPoint = new HashMap<>();
            dataPoint.put("date", date.toLocalDate().toString());
            dataPoint.put("count", count);
            growthData.add(dataPoint);
        }

        return growthData;
    }

    public Long getUserRank(Long userId) {
        UserProgress userProgress = getUserProgress(userId);
        List<UserProgress> allProgress = userProgressRepository.findAll();

        long rank = allProgress.stream()
                .filter(progress -> progress.getSolvedCount() > userProgress.getSolvedCount())
                .count() + 1;

        return rank;
    }
}
