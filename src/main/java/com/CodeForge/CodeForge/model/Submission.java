package com.codeforge.codeforge.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "submissions")
public class Submission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @NotNull
    @Column(columnDefinition = "TEXT", nullable = false)
    private String code;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "execution_time")
    private Integer executionTime; // in milliseconds

    @Column(name = "memory_used")
    private Integer memoryUsed; // in KB

    @Column(name = "passed_test_cases")
    private Integer passedTestCases = 0;

    @Column(name = "total_test_cases")
    private Integer totalTestCases = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    // Enums
    public enum Language {
        JAVA, PYTHON, JAVASCRIPT, CPP, C, CSHARP, RUBY, GO, RUST, SWIFT, KOTLIN, TYPESCRIPT
    }

    public enum Status {
        PENDING, RUNNING, ACCEPTED, WRONG_ANSWER, TIME_LIMIT_EXCEEDED,
        COMPILATION_ERROR, RUNTIME_ERROR, MEMORY_LIMIT_EXCEEDED
    }

    // Constructors
    public Submission() {}

    public Submission(User user, Problem problem, String code, Language language) {
        this.user = user;
        this.problem = problem;
        this.code = code;
        this.language = language;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public Problem getProblem() { return problem; }
    public void setProblem(Problem problem) { this.problem = problem; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Language getLanguage() { return language; }
    public void setLanguage(Language language) { this.language = language; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Integer getExecutionTime() { return executionTime; }
    public void setExecutionTime(Integer executionTime) { this.executionTime = executionTime; }

    public Integer getMemoryUsed() { return memoryUsed; }
    public void setMemoryUsed(Integer memoryUsed) { this.memoryUsed = memoryUsed; }

    public Integer getPassedTestCases() { return passedTestCases; }
    public void setPassedTestCases(Integer passedTestCases) { this.passedTestCases = passedTestCases; }

    public Integer getTotalTestCases() { return totalTestCases; }
    public void setTotalTestCases(Integer totalTestCases) { this.totalTestCases = totalTestCases; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    // Helper methods
    public boolean isAccepted() {
        return status == Status.ACCEPTED;
    }

    public boolean isPending() {
        return status == Status.PENDING || status == Status.RUNNING;
    }

    public boolean isFailed() {
        return !isAccepted() && !isPending();
    }

    public double getSuccessRate() {
        if (totalTestCases == 0) return 0.0;
        return (passedTestCases * 100.0) / totalTestCases;
    }

    @Override
    public String toString() {
        return "Submission{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : "null") +
                ", problemId=" + (problem != null ? problem.getId() : "null") +
                ", status=" + status +
                ", language=" + language +
                '}';
    }
}