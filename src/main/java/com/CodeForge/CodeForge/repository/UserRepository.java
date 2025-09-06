package com.codeforge.codeforge.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.codeforge.codeforge.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Basic find operations
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    
    // Existence checks (for registration validation)
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    // Find by role
    List<User> findByRole(User.Role role);
    
    // Reputation-based queries
    List<User> findByReputationGreaterThanEqual(Integer minReputation);
    List<User> findByReputationLessThanEqual(Integer maxReputation);
    List<User> findTop10ByOrderByReputationDesc();
    
    // Streak-based queries
    List<User> findByStreakCountGreaterThan(Integer minStreak);
    List<User> findByStreakCount(Integer streakCount);
    
    // Activity-based queries
    List<User> findByLastActivityDate(LocalDate date);
    List<User> findByLastActivityDateAfter(LocalDate date);
    List<User> findByLastActivityDateBefore(LocalDate date);
    
    // Date-based queries
    List<User> findByCreatedAtAfter(LocalDateTime date);
    List<User> findByCreatedAtBefore(LocalDateTime date);
    List<User> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    
    // Name-based searches
    List<User> findByFirstNameContainingIgnoreCase(String firstName);
    List<User> findByLastNameContainingIgnoreCase(String lastName);
    List<User> findByFirstNameAndLastName(String firstName, String lastName);
    
    // Advanced search query
    @Query("SELECT u FROM User u WHERE " +
           "LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.email) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.firstName) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(u.lastName) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<User> searchUsers(@Param("query") String query);
    
    // Count operations
    long countByRole(User.Role role);
    long countByReputationGreaterThanEqual(Integer minReputation);
    long countByLastActivityDate(LocalDate date);
    
    // Statistics queries
    @Query("SELECT AVG(u.reputation) FROM User u")
    Double findAverageReputation();
    
    @Query("SELECT MAX(u.reputation) FROM User u")
    Integer findMaxReputation();
    
    @Query("SELECT MIN(u.reputation) FROM User u")
    Integer findMinReputation();
    
    @Query("SELECT COUNT(u) FROM User u WHERE u.lastActivityDate >= :date")
    long countActiveUsersSince(@Param("date") LocalDate date);
}