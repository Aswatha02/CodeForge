package com.CodeForge.CodeForge.dto;

import java.util.List;

public class SubmissionResult {

    private Long submissionId;
    private String verdict; // ACCEPTED, WRONG_ANSWER, TIME_LIMIT_EXCEEDED, RUNTIME_ERROR, COMPILATION_ERROR
    private Integer executionTime; // in milliseconds
    private Integer memoryUsed; // in KB
    private Integer totalTestCases;
    private Integer passedTestCases;
    private String errorMessage;
    private List<TestCaseResult> testCaseResults;

    // Constructors
    public SubmissionResult() {}

    public SubmissionResult(Long submissionId, String verdict) {
        this.submissionId = submissionId;
        this.verdict = verdict;
    }

    // Getters and Setters
    public Long getSubmissionId() { return submissionId; }
    public void setSubmissionId(Long submissionId) { this.submissionId = submissionId; }

    public String getVerdict() { return verdict; }
    public void setVerdict(String verdict) { this.verdict = verdict; }

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

    public List<TestCaseResult> getTestCaseResults() { return testCaseResults; }
    public void setTestCaseResults(List<TestCaseResult> testCaseResults) { this.testCaseResults = testCaseResults; }

    // Inner class for individual test case results
    public static class TestCaseResult {
        private Long testCaseId;
        private String input;
        private String expectedOutput;
        private String actualOutput;
        private String status; // PASSED, FAILED
        private Integer executionTime;
        private Integer memoryUsed;
        private String errorMessage;

        public TestCaseResult() {}

        public TestCaseResult(Long testCaseId, String status) {
            this.testCaseId = testCaseId;
            this.status = status;
        }

        // Getters and Setters
        public Long getTestCaseId() { return testCaseId; }
        public void setTestCaseId(Long testCaseId) { this.testCaseId = testCaseId; }

        public String getInput() { return input; }
        public void setInput(String input) { this.input = input; }

        public String getExpectedOutput() { return expectedOutput; }
        public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }

        public String getActualOutput() { return actualOutput; }
        public void setActualOutput(String actualOutput) { this.actualOutput = actualOutput; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public Integer getExecutionTime() { return executionTime; }
        public void setExecutionTime(Integer executionTime) { this.executionTime = executionTime; }

        public Integer getMemoryUsed() { return memoryUsed; }
        public void setMemoryUsed(Integer memoryUsed) { this.memoryUsed = memoryUsed; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}
