package com.CodeForge.CodeForge.dto;

import com.CodeForge.CodeForge.model.User;
import com.CodeForge.CodeForge.model.UserProgress;

import java.time.LocalDateTime;

public class LeaderboardEntryDTO {
    private Long id;
    private String username;
    private String email;
    private Integer problemsSolved;
    private Integer totalScore;
    private Double acceptanceRate;
    private LocalDateTime lastActive;
    private Integer solvedEasyCount;
    private Integer solvedMediumCount;
    private Integer solvedHardCount;
    private Integer currentStreak;
    private Integer maxStreak;

    public LeaderboardEntryDTO() {}

    public LeaderboardEntryDTO(User user, UserProgress progress) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.email = user.getEmail();
        this.problemsSolved = progress.getSolvedCount();
        this.totalScore = calculateTotalScore(progress);
        this.acceptanceRate = progress.getAcceptanceRate();
        this.lastActive = progress.getLastActivityDate();
        this.solvedEasyCount = progress.getSolvedEasyCount();
        this.solvedMediumCount = progress.getSolvedMediumCount();
        this.solvedHardCount = progress.getSolvedHardCount();
        this.currentStreak = progress.getCurrentStreak();
        this.maxStreak = progress.getMaxStreak();
    }

    private Integer calculateTotalScore(UserProgress progress) {
        // Score calculation: Easy = 100, Medium = 200, Hard = 300
        return (progress.getSolvedEasyCount() * 100) +
               (progress.getSolvedMediumCount() * 200) +
               (progress.getSolvedHardCount() * 300);
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getProblemsSolved() { return problemsSolved; }
    public void setProblemsSolved(Integer problemsSolved) { this.problemsSolved = problemsSolved; }

    public Integer getTotalScore() { return totalScore; }
    public void setTotalScore(Integer totalScore) { this.totalScore = totalScore; }

    public Double getAcceptanceRate() { return acceptanceRate; }
    public void setAcceptanceRate(Double acceptanceRate) { this.acceptanceRate = acceptanceRate; }

    public LocalDateTime getLastActive() { return lastActive; }
    public void setLastActive(LocalDateTime lastActive) { this.lastActive = lastActive; }

    public Integer getSolvedEasyCount() { return solvedEasyCount; }
    public void setSolvedEasyCount(Integer solvedEasyCount) { this.solvedEasyCount = solvedEasyCount; }

    public Integer getSolvedMediumCount() { return solvedMediumCount; }
    public void setSolvedMediumCount(Integer solvedMediumCount) { this.solvedMediumCount = solvedMediumCount; }

    public Integer getSolvedHardCount() { return solvedHardCount; }
    public void setSolvedHardCount(Integer solvedHardCount) { this.solvedHardCount = solvedHardCount; }

    public Integer getCurrentStreak() { return currentStreak; }
    public void setCurrentStreak(Integer currentStreak) { this.currentStreak = currentStreak; }

    public Integer getMaxStreak() { return maxStreak; }
    public void setMaxStreak(Integer maxStreak) { this.maxStreak = maxStreak; }
}
