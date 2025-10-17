package com.CodeForge.CodeForge.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;


@Entity
@Table(name = "user_progress")
public class UserProgress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "solved_count", nullable = false)
    private Integer solvedCount = 0;

    @Column(name = "solved_easy_count", nullable = false)
    private Integer solvedEasyCount = 0;

    @Column(name = "solved_medium_count", nullable = false)
    private Integer solvedMediumCount = 0;

    @Column(name = "solved_hard_count", nullable = false)
    private Integer solvedHardCount = 0;

    @Column(name = "total_submissions", nullable = false)
    private Integer totalSubmissions = 0;

    @Column(name = "accepted_submissions", nullable = false)
    private Integer acceptedSubmissions = 0;

    @Column(name = "current_streak", nullable = false)
    private Integer currentStreak = 0;

    @Column(name = "max_streak", nullable = false)
    private Integer maxStreak = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    // Getters
    public Long getId() { return id; }
    public User getUser() { return user; }
    public Integer getSolvedCount() { return solvedCount; }
    public Integer getSolvedEasyCount() { return solvedEasyCount; }
    public Integer getSolvedMediumCount() { return solvedMediumCount; }
    public Integer getSolvedHardCount() { return solvedHardCount; }
    public Integer getTotalSubmissions() { return totalSubmissions; }
    public Integer getAcceptedSubmissions() { return acceptedSubmissions; }
    public Integer getCurrentStreak() { return currentStreak; }
    public Integer getMaxStreak() { return maxStreak; }
    public Status getStatus() { return status; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setUser(User user) { this.user = user; }
    public void setSolvedCount(Integer solvedCount) { this.solvedCount = solvedCount; }
    public void setSolvedEasyCount(Integer solvedEasyCount) { this.solvedEasyCount = solvedEasyCount; }
    public void setSolvedMediumCount(Integer solvedMediumCount) { this.solvedMediumCount = solvedMediumCount; }
    public void setSolvedHardCount(Integer solvedHardCount) { this.solvedHardCount = solvedHardCount; }
    public void setTotalSubmissions(Integer totalSubmissions) { this.totalSubmissions = totalSubmissions; }
    public void setAcceptedSubmissions(Integer acceptedSubmissions) { this.acceptedSubmissions = acceptedSubmissions; }
    public void setCurrentStreak(Integer currentStreak) { this.currentStreak = currentStreak; }
    public void setMaxStreak(Integer maxStreak) { this.maxStreak = maxStreak; }
    public void setStatus(Status status) { this.status = status; }

    

    public enum Status {
        ACTIVE, INACTIVE
    }
}