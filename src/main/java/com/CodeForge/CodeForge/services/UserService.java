package com.CodeForge.CodeForge.services;

import com.CodeForge.CodeForge.model.User;
import com.CodeForge.CodeForge.model.UserProgress;
import com.CodeForge.CodeForge.repository.UserProgressRepository;
import com.CodeForge.CodeForge.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final UserProgressRepository userProgressRepository;
    private final PasswordEncoder passwordEncoder;

    // ✅ Inline creation of PasswordEncoder instead of autowiring
    public UserService(UserRepository userRepository,
                       UserProgressRepository userProgressRepository) {
        this.userRepository = userRepository;
        this.userProgressRepository = userProgressRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    // ✅ Register new user (with password hashing)
    public User register(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email already exists");
        }
        // Hash the password before saving
        user.setPasswordHash(passwordEncoder.encode(user.getPasswordHash()));
        return userRepository.save(user);
    }

    // ✅ Login user (check raw password vs hashed password)
    public User login(String usernameOrEmail, String password) {
        Optional<User> userOpt = userRepository.findByUsername(usernameOrEmail);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(usernameOrEmail);
        }
        User user = userOpt.orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }
        return user;
    }

    // ✅ Get user progress
    public List<UserProgress> getUserProgress(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return userProgressRepository.findByUser(user);
    }
}
