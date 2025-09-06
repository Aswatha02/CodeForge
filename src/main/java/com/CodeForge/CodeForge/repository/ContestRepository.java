package com.codeforge.codeforge.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.codeforge.codeforge.model.Contest;
import com.codeforge.codeforge.model.User;

@Repository
public interface ContestRepository extends JpaRepository<Contest, Long> {

    // Find contests by status
    List<Contest> findByStatus(Contest.Status status);

    // Find upcoming contests
    List<Contest> findByStartTimeAfter(LocalDateTime now);

    // Find running contests
    @Query("SELECT c FROM Contest c WHERE c.startTime <= :now AND c.endTime >= :now")
    List<Contest> findRunningContests(LocalDateTime now);

    // Find contests created by a specific user
    List<Contest> findByCreatedBy(User user);

    // Find contests that are public
    List<Contest> findByIsPublicTrue();

    // Find contests that are completed
    List<Contest> findByStatusOrderByEndTimeDesc(Contest.Status status);
}
