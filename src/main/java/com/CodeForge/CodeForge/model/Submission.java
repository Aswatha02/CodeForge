package com.CodeForge.CodeForge.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "submissions")
public class Submission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

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
    private Integer executionTime; // in milliseconds

    @Column(name = "memory_used")
    private Integer memoryUsed; // in MB

    @Column(name = "total_test_cases", nullable = false)
    private int totalTestCases = 0;

    @Column(name = "passed_test_cases", nullable = false)
    private int passedTestCases = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "submitted_at", nullable = false, updatable = false)
    private LocalDateTime submittedAt = LocalDateTime.now();

    @Column(name = "expected_output", columnDefinition = "TEXT")
    private String expectedOutput;

    @Column(name = "actual_output", columnDefinition = "TEXT")
    private String actualOutput;

    // Custom method for checking if submission is accepted
    public boolean isAccepted() {
        return Status.ACCEPTED.equals(this.status);
    }

    public void setStatus(Status status) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Status getStatus() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setErrorMessage(String string) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setTotalTestCases(int size) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setPassedTestCases(int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }


        // Add these methods to your Submission class
    public String getCode() { return this.code; }
    public Submission.Language getLanguage() { return this.language; }
    public Integer getPassedTestCases() { return this.passedTestCases; }
    public Integer getTotalTestCases() { return this.totalTestCases; }
    public Problem getProblem() { return this.problem; }

    public void setPassedTestCases(Integer passedTestCases) { this.passedTestCases = passedTestCases; }
    public void setTotalTestCases(Integer totalTestCases) { this.totalTestCases = totalTestCases; }
    public void setExecutionTime(Integer executionTime) { this.executionTime = executionTime; }
    public void setMemoryUsed(Integer memoryUsed) { this.memoryUsed = memoryUsed; }
    public void setActualOutput(String actualOutput) { this.actualOutput = actualOutput; }
    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }

    public void setProblem(Problem problem) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setSubmittedAt(LocalDateTime now) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setLanguage(Language language) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setCode(String code) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setUser(User user) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    public enum Language {
        JAVA, JAVASCRIPT, PYTHON, CPP, C
    }

    public enum Status {
        PENDING, ACCEPTED, WRONG_ANSWER, TIME_LIMIT_EXCEEDED,
        RUNTIME_ERROR, COMPILATION_ERROR
    }

    // Convenience constructor for creating a new submission (matches the service call at line 89)
    /*
    public Submission() {
        this.user = user;
        this.problem = problem;
        this.code = code;
        this.language = language;
        this.status = Status.PENDING;
        this.submittedAt = LocalDateTime.now();
    }*/
}
