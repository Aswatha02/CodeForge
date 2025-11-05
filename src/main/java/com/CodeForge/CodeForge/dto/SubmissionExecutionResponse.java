package com.CodeForge.CodeForge.dto;

import java.util.List;

public class SubmissionExecutionResponse {
    private List<TestCaseResult> testCaseResults;
    private Integer totalExecutionTime;
    private Integer maxMemoryUsed;
    private String error;

    public static class TestCaseResult {
        private Long testCaseId;
        private boolean passed;
        private boolean timedOut;
        private String actualOutput;
        private String error;

        // Getters and setters
        public Long getTestCaseId() { return testCaseId; }
        public void setTestCaseId(Long testCaseId) { this.testCaseId = testCaseId; }

        public boolean isPassed() { return passed; }
        public void setPassed(boolean passed) { this.passed = passed; }

        public boolean isTimedOut() { return timedOut; }
        public void setTimedOut(boolean timedOut) { this.timedOut = timedOut; }

        public String getActualOutput() { return actualOutput; }
        public void setActualOutput(String actualOutput) { this.actualOutput = actualOutput; }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
    }

    // Getters and setters
    public List<TestCaseResult> getTestCaseResults() { return testCaseResults; }
    public void setTestCaseResults(List<TestCaseResult> testCaseResults) { this.testCaseResults = testCaseResults; }

    public Integer getTotalExecutionTime() { return totalExecutionTime; }
    public void setTotalExecutionTime(Integer totalExecutionTime) { this.totalExecutionTime = totalExecutionTime; }

    public Integer getMaxMemoryUsed() { return maxMemoryUsed; }
    public void setMaxMemoryUsed(Integer maxMemoryUsed) { this.maxMemoryUsed = maxMemoryUsed; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }
}
