package com.CodeForge.CodeForge.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "submissions")
public class Submission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String code;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @Column(name = "execution_time")
    private Integer executionTime;

    @Column(name = "memory_used")
    private Integer memoryUsed;

    @Column(name = "total_test_cases", nullable = false)
    private Integer totalTestCases = 0;

    @Column(name = "passed_test_cases", nullable = false)
    private Integer passedTestCases = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(name = "expected_output", columnDefinition = "TEXT")
    private String expectedOutput;

    @Column(name = "actual_output", columnDefinition = "TEXT")
    private String actualOutput;

    // Constructors
    public Submission() {}

    public Submission(User user, Problem problem, String code, Language language) {
        this.user = user;
        this.problem = problem;
        this.code = code;
        this.language = language;
        this.status = Status.PENDING;
        this.submittedAt = LocalDateTime.now();
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

    public Integer getTotalTestCases() { return totalTestCases; }
    public void setTotalTestCases(Integer totalTestCases) { this.totalTestCases = totalTestCases; }

    public Integer getPassedTestCases() { return passedTestCases; }
    public void setPassedTestCases(Integer passedTestCases) { this.passedTestCases = passedTestCases; }

    public String getErrorMessage() { return errorMessage; }
    public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

    public LocalDateTime getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(LocalDateTime submittedAt) { this.submittedAt = submittedAt; }

    public String getExpectedOutput() { return expectedOutput; }
    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }

    public String getActualOutput() { return actualOutput; }
    public void setActualOutput(String actualOutput) { this.actualOutput = actualOutput; }

    // Custom method for checking if submission is accepted
    public boolean isAccepted() {
        return Status.ACCEPTED.equals(this.status);
    }

    public enum Language {
        JAVA, JAVASCRIPT, PYTHON, CPP, C
    }

    public enum Status {
        PENDING, ACCEPTED, WRONG_ANSWER, TIME_LIMIT_EXCEEDED,
        RUNTIME_ERROR, COMPILATION_ERROR
    }
}