package com.CodeForge.CodeForge.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.CodeForge.CodeForge.model.Contest;
import com.CodeForge.CodeForge.model.User;

@Repository
public interface ContestRepository extends JpaRepository<Contest, Long> {

    // Find contests by status
    List<Contest> findByStatus(Contest.Status status);

    // Find upcoming contests
    List<Contest> findByStartTimeAfter(LocalDateTime now);

    boolean existsByid(Long id);

    // Find running contests
    @Query("SELECT c FROM Contest c WHERE c.startTime <= :now AND c.endTime >= :now")
    List<Contest> findRunningContests(@Param("now") LocalDateTime now);

    // Find contests created by a specific user
    List<Contest> findByCreatedBy(User user);

    // Find contests that are public
    List<Contest> findByIsPublicTrue();

    // Find contests that are completed
    List<Contest> findByStatusOrderByEndTimeDesc(Contest.Status status);

    // Count contests by status
    long countByStatus(Contest.Status status);
    
    // Find active contests (RUNNING status)
     @Query("SELECT c FROM Contest c WHERE c.status = 'RUNNING'")
    List<Contest> findActiveContests();
}