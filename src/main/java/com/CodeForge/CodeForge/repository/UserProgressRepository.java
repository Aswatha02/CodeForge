package com.CodeForge.CodeForge.repository;

import com.CodeForge.CodeForge.model.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {
    
    Optional<UserProgress> findByUserId(Long userId);
    
    @Query("SELECT up FROM UserProgress up JOIN FETCH up.user WHERE up.user.id = :userId")
    Optional<UserProgress> findWithUser(@Param("userId") Long userId);
}