package com.CodeForge.CodeForge.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "test_cases")
public class TestCase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // CHANGED: Input as JSON to handle multiple parameters
    @NotBlank(message = "Input data is required")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String inputData; // Store as JSON: {"nums": [2,7,11,15], "target": 9}

    @NotBlank(message = "Expected output is required")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String expectedOutput;

    @Column(name = "is_sample", nullable = false)
    private Boolean isSample = true;

    // NEW: Explanation for the test case
    @Column(columnDefinition = "TEXT")
    private String explanation;

    // NEW: Test case name for better organization
    @Column(name = "test_case_name")
    private String testCaseName;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    // Getters
    public Long getId() {
        return this.id;
    }

    public String getInputData() { 
        return this.inputData; 
    }

    public String getExpectedOutput() { 
        return this.expectedOutput; 
    }

    public Boolean getIsSample() {
        return this.isSample;
    }

    public String getExplanation() {
        return this.explanation;
    }

    public String getTestCaseName() {
        return this.testCaseName;
    }

    public Problem getProblem() {
        return this.problem;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setInputData(String inputData) {
        this.inputData = inputData;
    }

    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }

    public void setIsSample(Boolean isSample) {
        this.isSample = isSample;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public void setTestCaseName(String testCaseName) {
        this.testCaseName = testCaseName;
    }

    public void setProblem(Problem problem) { 
        this.problem = problem; 
    }

    // equals and hashCode methods
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof TestCase)) return false;
        TestCase testCase = (TestCase) o;
        return id != null && id.equals(testCase.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

    // toString method
    @Override
    public String toString() {
        return "TestCase{" +
                "id=" + id +
                ", inputData='" + inputData + '\'' +
                ", expectedOutput='" + expectedOutput + '\'' +
                ", isSample=" + isSample +
                ", explanation='" + explanation + '\'' +
                ", testCaseName='" + testCaseName + '\'' +
                '}';
    }
}