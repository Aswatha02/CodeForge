package com.CodeForge.CodeForge.dto;

import java.util.Objects;

public class ProblemStats {
    private Long problemId;
    private Integer testCaseCount;
    private String difficulty;

    // Constructors
    public ProblemStats() {
    }

    public ProblemStats(Long problemId, Integer testCaseCount, String difficulty) {
        this.problemId = problemId;
        this.testCaseCount = testCaseCount;
        this.difficulty = difficulty;
    }

    // Getters
    public Long getProblemId() {
        return problemId;
    }

    public Integer getTestCaseCount() {
        return testCaseCount;
    }

    public String getDifficulty() {
        return difficulty;
    }

    // Setters
    public void setProblemId(Long problemId) {
        this.problemId = problemId;
    }

    public void setTestCaseCount(Integer testCaseCount) {
        this.testCaseCount = testCaseCount;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    // equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProblemStats that = (ProblemStats) o;
        return Objects.equals(problemId, that.problemId) &&
               Objects.equals(testCaseCount, that.testCaseCount) &&
               Objects.equals(difficulty, that.difficulty);
    }

    @Override
    public int hashCode() {
        return Objects.hash(problemId, testCaseCount, difficulty);
    }

    // toString
    @Override
    public String toString() {
        return "ProblemStats{" +
                "problemId=" + problemId +
                ", testCaseCount=" + testCaseCount +
                ", difficulty='" + difficulty + '\'' +
                '}';
    }
}