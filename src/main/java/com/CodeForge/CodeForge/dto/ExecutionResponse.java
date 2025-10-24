package com.CodeForge.CodeForge.dto;

import java.util.List;

public class ExecutionResponse {

    private String status; // SUCCESS, COMPILATION_ERROR, etc.
    private Integer totalExecutionTime; // in milliseconds
    private Integer maxMemoryUsed; // in KB
    private List<TestCaseExecutionResult> testCaseResults;
    private String compilationError;
    private String runtimeError;

    // Constructors
    public ExecutionResponse() {}

    public ExecutionResponse(String status) {
        this.status = status;
    }

    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Integer getTotalExecutionTime() { return totalExecutionTime; }
    public void setTotalExecutionTime(Integer totalExecutionTime) { this.totalExecutionTime = totalExecutionTime; }

    public Integer getMaxMemoryUsed() { return maxMemoryUsed; }
    public void setMaxMemoryUsed(Integer maxMemoryUsed) { this.maxMemoryUsed = maxMemoryUsed; }

    public List<TestCaseExecutionResult> getTestCaseResults() { return testCaseResults; }
    public void setTestCaseResults(List<TestCaseExecutionResult> testCaseResults) { this.testCaseResults = testCaseResults; }

    public String getCompilationError() { return compilationError; }
    public void setCompilationError(String compilationError) { this.compilationError = compilationError; }

    public String getRuntimeError() { return runtimeError; }
    public void setRuntimeError(String runtimeError) { this.runtimeError = runtimeError; }

    // Inner class for individual test case execution results
    public static class TestCaseExecutionResult {
        private Long testCaseId;
        private String status; // PASSED, FAILED, TIME_LIMIT_EXCEEDED, RUNTIME_ERROR
        private String actualOutput;
        private Integer executionTime; // in milliseconds
        private Integer memoryUsed; // in KB
        private String errorMessage;
        private Integer weight;

        public TestCaseExecutionResult() {}

        public TestCaseExecutionResult(Long testCaseId, String status) {
            this.testCaseId = testCaseId;
            this.status = status;
        }

        // Getters and Setters
        public Long getTestCaseId() { return testCaseId; }
        public void setTestCaseId(Long testCaseId) { this.testCaseId = testCaseId; }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public String getActualOutput() { return actualOutput; }
        public void setActualOutput(String actualOutput) { this.actualOutput = actualOutput; }

        public Integer getExecutionTime() { return executionTime; }
        public void setExecutionTime(Integer executionTime) { this.executionTime = executionTime; }

        public Integer getMemoryUsed() { return memoryUsed; }
        public void setMemoryUsed(Integer memoryUsed) { this.memoryUsed = memoryUsed; }

        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }

        public Integer getWeight() { return weight; }
        public void setWeight(Integer weight) { this.weight = weight; }
    }
}
