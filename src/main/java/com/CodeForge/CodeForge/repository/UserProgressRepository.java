package com.CodeForge.CodeForge.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.CodeForge.CodeForge.model.Problem;
import com.CodeForge.CodeForge.model.User;
import com.CodeForge.CodeForge.model.UserProgress;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {

    // Find progress for a specific user and problem
    Optional<UserProgress> findByUserAndProblem(User user, Problem problem);

    // Find all progress records for a user
    List<UserProgress> findByUser(User user);

    // Find all progress records for a user by status
    List<UserProgress> findByUserAndStatus(User user, UserProgress.Status status);

    // Count how many problems a user solved
    long countByUserAndStatus(User user, UserProgress.Status status);

    // Find all progress for a specific problem
    List<UserProgress> findByProblem(Problem problem);

    // Check if a user solved a specific problem
    boolean existsByUserAndProblemAndStatus(User user, Problem problem, UserProgress.Status status);
}
