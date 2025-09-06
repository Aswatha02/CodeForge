package com.codeforge.codeforge.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "test_cases")
public class TestCase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @NotNull
    @Column(columnDefinition = "TEXT", nullable = false)
    private String inputData;

    @NotNull
    @Column(columnDefinition = "TEXT", nullable = false)
    private String expectedOutput;

    @Column(name = "is_sample")
    private Boolean isSample = false;

    @Column(name = "is_hidden")
    private Boolean isHidden = false;

    @Column(columnDefinition = "TEXT")
    private String explanation;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // JPA Lifecycle callback for updatedAt
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    

    // Constructors
    public TestCase() {}

    public TestCase(Problem problem, String inputData, String expectedOutput, Boolean isSample, Boolean isHidden) {
        this.problem = problem;
        this.inputData = inputData;
        this.expectedOutput = expectedOutput;
        this.isSample = isSample;
        this.isHidden = isHidden;
    }

    public TestCase(Problem problem, String inputData, String expectedOutput, Boolean isSample, 
                   Boolean isHidden, String explanation) {
        this(problem, inputData, expectedOutput, isSample, isHidden);
        this.explanation = explanation;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Problem getProblem() { return problem; }
    public void setProblem(Problem problem) { this.problem = problem; }

    public String getInputData() { return inputData; }
    public void setInputData(String inputData) { this.inputData = inputData; }

    public String getExpectedOutput() { return expectedOutput; }
    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }

    public Boolean getIsSample() { return isSample; }
    public void setIsSample(Boolean isSample) { this.isSample = isSample; }

    public Boolean getIsHidden() { return isHidden; }
    public void setIsHidden(Boolean isHidden) { this.isHidden = isHidden; }

    public String getExplanation() { return explanation; }
    public void setExplanation(String explanation) { this.explanation = explanation; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public boolean isVisibleToUser() {
        return !isHidden || isSample;
    }

    public boolean requiresExactMatch() {
        // For most coding problems, output must match exactly
        return true;
    }

    @Override
    public String toString() {
        return "TestCase{" +
                "id=" + id +
                ", problemId=" + (problem != null ? problem.getId() : "null") +
                ", isSample=" + isSample +
                ", isHidden=" + isHidden +
                '}';
    }
}