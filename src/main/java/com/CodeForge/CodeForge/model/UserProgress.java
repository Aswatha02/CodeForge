package com.CodeForge.CodeForge.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

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

    // Track specific solved problems (problems where user has at least one ACCEPTED submission)
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "user_solved_problems",
        joinColumns = @JoinColumn(name = "user_progress_id"),
        inverseJoinColumns = @JoinColumn(name = "problem_id")
    )
    @JsonIgnore
    private Set<Problem> solvedProblems = new HashSet<>();

    // Aggregate statistics - calculated from submissions
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

    @Column(name = "last_activity_date")
    private LocalDateTime lastActivityDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    // Constructors
    public UserProgress() {}

    public UserProgress(User user) {
        this.user = user;
    }

    // Business logic methods that work with your Submission entity
    public void updateFromSubmission(Submission submission, Problem problem) {
        totalSubmissions++;
        
        if (submission.getStatus() == Submission.Status.ACCEPTED) {
            acceptedSubmissions++;
            
            // Add to solved problems if this is the first accepted submission for this problem
            if (!hasSolvedProblem(problem)) {
                addSolvedProblem(problem);
            }
        }
        
        updateStreak();
        updatedAt = LocalDateTime.now();
    }

    public void addSolvedProblem(Problem problem) {
        if (solvedProblems.add(problem)) {
            solvedCount++;
            updateDifficultyCount(problem);
            updatedAt = LocalDateTime.now();
        }
    }

    public void removeSolvedProblem(Problem problem) {
        if (solvedProblems.remove(problem)) {
            solvedCount--;
            updateDifficultyCountOnRemove(problem);
            updatedAt = LocalDateTime.now();
        }
    }

    public boolean hasSolvedProblem(Long problemId) {
        return solvedProblems.stream()
                .anyMatch(problem -> problem.getId().equals(problemId));
    }

    public boolean hasSolvedProblem(Problem problem) {
        return solvedProblems.contains(problem);
    }

    public double getAcceptanceRate() {
        return totalSubmissions > 0 ? 
               (double) acceptedSubmissions / totalSubmissions * 100 : 0.0;
    }

    private void updateDifficultyCount(Problem problem) {
        // Use the Problem's difficulty enum directly
        if (problem.getDifficulty() == Problem.Difficulty.EASY) {
            solvedEasyCount++;
        } else if (problem.getDifficulty() == Problem.Difficulty.MEDIUM) {
            solvedMediumCount++;
        } else if (problem.getDifficulty() == Problem.Difficulty.HARD) {
            solvedHardCount++;
        }
    }

    private void updateDifficultyCountOnRemove(Problem problem) {
        // Use the Problem's difficulty enum directly
        if (problem.getDifficulty() == Problem.Difficulty.EASY) {
            solvedEasyCount = Math.max(0, solvedEasyCount - 1);
        } else if (problem.getDifficulty() == Problem.Difficulty.MEDIUM) {
            solvedMediumCount = Math.max(0, solvedMediumCount - 1);
        } else if (problem.getDifficulty() == Problem.Difficulty.HARD) {
            solvedHardCount = Math.max(0, solvedHardCount - 1);
        }
    }

    private void updateStreak() {
        LocalDateTime today = LocalDateTime.now();
        
        if (lastActivityDate != null) {
            LocalDateTime yesterday = today.minusDays(1);
            if (lastActivityDate.toLocalDate().equals(yesterday.toLocalDate())) {
                currentStreak++;
            } else if (!lastActivityDate.toLocalDate().equals(today.toLocalDate())) {
                currentStreak = 1;
            }
        } else {
            currentStreak = 1;
        }
        
        maxStreak = Math.max(maxStreak, currentStreak);
        lastActivityDate = today;
    }

    // Getters
    public Long getId() { return id; }
    public User getUser() { return user; }
    public Set<Problem> getSolvedProblems() { return solvedProblems; }
    public Integer getSolvedCount() { return solvedCount; }
    public Integer getSolvedEasyCount() { return solvedEasyCount; }
    public Integer getSolvedMediumCount() { return solvedMediumCount; }
    public Integer getSolvedHardCount() { return solvedHardCount; }
    public Integer getTotalSubmissions() { return totalSubmissions; }
    public Integer getAcceptedSubmissions() { return acceptedSubmissions; }
    public Integer getCurrentStreak() { return currentStreak; }
    public Integer getMaxStreak() { return maxStreak; }
    public LocalDateTime getLastActivityDate() { return lastActivityDate; }
    public Status getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    // Setters
    public void setId(Long id) { this.id = id; }
    public void setUser(User user) { this.user = user; }
    public void setSolvedProblems(Set<Problem> solvedProblems) { this.solvedProblems = solvedProblems; }
    public void setSolvedCount(Integer solvedCount) { this.solvedCount = solvedCount; }
    public void setSolvedEasyCount(Integer solvedEasyCount) { this.solvedEasyCount = solvedEasyCount; }
    public void setSolvedMediumCount(Integer solvedMediumCount) { this.solvedMediumCount = solvedMediumCount; }
    public void setSolvedHardCount(Integer solvedHardCount) { this.solvedHardCount = solvedHardCount; }
    public void setTotalSubmissions(Integer totalSubmissions) { this.totalSubmissions = totalSubmissions; }
    public void setAcceptedSubmissions(Integer acceptedSubmissions) { this.acceptedSubmissions = acceptedSubmissions; }
    public void setCurrentStreak(Integer currentStreak) { this.currentStreak = currentStreak; }
    public void setMaxStreak(Integer maxStreak) { this.maxStreak = maxStreak; }
    public void setLastActivityDate(LocalDateTime lastActivityDate) { this.lastActivityDate = lastActivityDate; }
    public void setStatus(Status status) { this.status = status; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public enum Status {
        ACTIVE, INACTIVE
    }
}