package com.CodeForge.CodeForge.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.CodeForge.CodeForge.model.UserProgress;
import com.CodeForge.CodeForge.model.User;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Long> {

    Optional<UserProgress> findById(Long userId);

    @Query("SELECT up FROM UserProgress up JOIN FETCH up.user WHERE up.user.id = :userId")
    Optional<UserProgress> findWithUser(@Param("userId") Long userId);

    Optional<UserProgress> findByUser(User user);
}
