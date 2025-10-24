package com.CodeForge.CodeForge.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.CodeForge.CodeForge.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.username = :usernameOrEmail OR u.email = :usernameOrEmail")
    Optional<User> findByUsernameOrEmail(@Param("usernameOrEmail") String usernameOrEmail);
    
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userProgress WHERE u.id = :id")
    Optional<User> findByIdWithProgress(@Param("id") Long id);

    @Query("SELECT COUNT(u) FROM User u WHERE u.status = 'ACTIVE'")
    long countActiveUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.status = 'BANNED'")
    long countBannedUsers();

    @Query("SELECT COUNT(u) FROM User u WHERE u.lastLogin >= :since")
    long countUsersLoggedInSince(@Param("since") java.time.LocalDateTime since);

    @Query("SELECT u FROM User u WHERE u.status = :status")
    java.util.List<User> findByStatus(@Param("status") User.Status status);

    @Query("SELECT u FROM User u WHERE u.role = :role")
    java.util.List<User> findByRole(@Param("role") User.Role role);

    @Query("SELECT u FROM User u WHERE u.lastLogin IS NOT NULL ORDER BY u.lastLogin DESC")
    java.util.List<User> findUsersOrderedByLastLogin();

    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%'))")
    java.util.List<User> findByUsernameOrEmailContaining(@Param("search") String search);





    

    
}