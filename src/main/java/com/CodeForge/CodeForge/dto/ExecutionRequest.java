package com.CodeForge.CodeForge.dto;

import java.util.List;

public class ExecutionRequest {

    private String combinedCode; // User code + hidden wrapper
    private String language; // JAVA, PYTHON, CPP, etc.
    private List<TestCaseExecution> testCases;
    private Integer timeLimitMs; // Time limit in milliseconds
    private Integer memoryLimitMb; // Memory limit in MB

    // Constructors
    public ExecutionRequest() {}

    public ExecutionRequest(String combinedCode, String language, List<TestCaseExecution> testCases) {
        this.combinedCode = combinedCode;
        this.language = language;
        this.testCases = testCases;
    }

    // Getters and Setters
    public String getCombinedCode() { return combinedCode; }
    public void setCombinedCode(String combinedCode) { this.combinedCode = combinedCode; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    public List<TestCaseExecution> getTestCases() { return testCases; }
    public void setTestCases(List<TestCaseExecution> testCases) { this.testCases = testCases; }

    public Integer getTimeLimitMs() { return timeLimitMs; }
    public void setTimeLimitMs(Integer timeLimitMs) { this.timeLimitMs = timeLimitMs; }

    public Integer getMemoryLimitMb() { return memoryLimitMb; }
    public void setMemoryLimitMb(Integer memoryLimitMb) { this.memoryLimitMb = memoryLimitMb; }

    // Inner class for test case execution data
    public static class TestCaseExecution {
        private Long testCaseId;
        private String inputData;
        private String expectedOutput;
        private Integer weight;

        public TestCaseExecution() {}

        public TestCaseExecution(Long testCaseId, String inputData, String expectedOutput, Integer weight) {
            this.testCaseId = testCaseId;
            this.inputData = inputData;
            this.expectedOutput = expectedOutput;
            this.weight = weight;
        }

        // Getters and Setters
        public Long getTestCaseId() { return testCaseId; }
        public void setTestCaseId(Long testCaseId) { this.testCaseId = testCaseId; }

        public String getInputData() { return inputData; }
        public void setInputData(String inputData) { this.inputData = inputData; }

        public String getExpectedOutput() { return expectedOutput; }
        public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }

        public Integer getWeight() { return weight; }
        public void setWeight(Integer weight) { this.weight = weight; }
    }
}
