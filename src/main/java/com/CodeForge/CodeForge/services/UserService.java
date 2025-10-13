package com.CodeForge.CodeForge.services;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.CodeForge.CodeForge.model.User;
import com.CodeForge.CodeForge.model.UserProgress;
import com.CodeForge.CodeForge.repository.UserRepository;
<<<<<<< HEAD
=======
import com.CodeForge.CodeForge.repository.UserProgressRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
>>>>>>> 7eacae3b011a02252ecc82d768f012602d1999c5

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final UserProgressRepository userProgressRepository;

<<<<<<< HEAD
    
    public UserService(UserRepository userRepository,
                       UserProgressRepository userProgressRepository) {
        this.userRepository = userRepository;
        this.userProgressRepository = userProgressRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    
    public User register(User user) {
=======
    @Transactional
    public User registerUser(User user) {
>>>>>>> 7eacae3b011a02252ecc82d768f012602d1999c5
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        
        User savedUser = userRepository.save(user);
        
        // Create user progress
        UserProgress progress = new UserProgress();
        progress.setUser(savedUser);
        userProgressRepository.save(progress);
        
        return savedUser; // REMOVED .getSafeUser() - just return the user
    }

    public User loginUser(String usernameOrEmail, String password) {
        Optional<User> userOpt = userRepository.findByUsernameOrEmail(usernameOrEmail);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        
        User user = userOpt.get();
        if (!user.getPasswordHash().equals(password)) {
            throw new RuntimeException("Invalid password");
        }
        
        return user; // REMOVED .getSafeUser() - just return the user
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id); // REMOVED .map(User::getSafeUser)
    }

    public List<User> getAllUsers() {
        return userRepository.findAll(); // REMOVED .stream().map(User::getSafeUser).toList()
    }

    public UserProgress getUserProgress(Long userId) {
        return userProgressRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("User progress not found"));
    }

    @Transactional
    public UserProgress updateUserProgress(Long userId, UserProgress progressUpdate) {
        UserProgress progress = getUserProgress(userId);
        
        if (progressUpdate.getSolvedCount() != null) {
            progress.setSolvedCount(progressUpdate.getSolvedCount());
        }
        if (progressUpdate.getSolvedEasyCount() != null) {
            progress.setSolvedEasyCount(progressUpdate.getSolvedEasyCount());
        }
        if (progressUpdate.getSolvedMediumCount() != null) {
            progress.setSolvedMediumCount(progressUpdate.getSolvedMediumCount());
        }
        if (progressUpdate.getSolvedHardCount() != null) {
            progress.setSolvedHardCount(progressUpdate.getSolvedHardCount());
        }
        if (progressUpdate.getTotalSubmissions() != null) {
            progress.setTotalSubmissions(progressUpdate.getTotalSubmissions());
        }
        if (progressUpdate.getAcceptedSubmissions() != null) {
            progress.setAcceptedSubmissions(progressUpdate.getAcceptedSubmissions());
        }
        if (progressUpdate.getCurrentStreak() != null) {
            progress.setCurrentStreak(progressUpdate.getCurrentStreak());
        }
        if (progressUpdate.getMaxStreak() != null) {
            progress.setMaxStreak(progressUpdate.getMaxStreak());
        }
        
        return userProgressRepository.save(progress);
    }

    @Transactional
    public void updateProgressAfterSubmission(Long userId, boolean accepted, String difficulty) {
        UserProgress progress = getUserProgress(userId);
        
        progress.setTotalSubmissions(progress.getTotalSubmissions() + 1);
        
        if (accepted) {
            progress.setAcceptedSubmissions(progress.getAcceptedSubmissions() + 1);
            progress.setSolvedCount(progress.getSolvedCount() + 1);
            
            if ("EASY".equals(difficulty)) {
                progress.setSolvedEasyCount(progress.getSolvedEasyCount() + 1);
            } else if ("MEDIUM".equals(difficulty)) {
                progress.setSolvedMediumCount(progress.getSolvedMediumCount() + 1);
            } else if ("HARD".equals(difficulty)) {
                progress.setSolvedHardCount(progress.getSolvedHardCount() + 1);
            }
            
            progress.setCurrentStreak(progress.getCurrentStreak() + 1);
            if (progress.getCurrentStreak() > progress.getMaxStreak()) {
                progress.setMaxStreak(progress.getCurrentStreak());
            }
        }
        
        userProgressRepository.save(progress);
    }
}