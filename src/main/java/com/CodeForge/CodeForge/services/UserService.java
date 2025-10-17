package com.CodeForge.CodeForge.services;

import com.CodeForge.CodeForge.model.User;
import com.CodeForge.CodeForge.model.UserProgress;
import com.CodeForge.CodeForge.repository.UserRepository;
import com.CodeForge.CodeForge.repository.UserProgressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserProgressRepository userProgressRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, 
                      UserProgressRepository userProgressRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.userProgressRepository = userProgressRepository;
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
                case "EASY":
                    progress.setSolvedEasyCount(progress.getSolvedEasyCount() + 1);
                    break;
                case "MEDIUM":
                    progress.setSolvedMediumCount(progress.getSolvedMediumCount() + 1);
                    break;
                case "HARD":
                    progress.setSolvedHardCount(progress.getSolvedHardCount() + 1);
                    break;
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

    public void deleteUser(Long userId) {
        User user = getUserById(userId);
        userRepository.delete(user);
    }
}