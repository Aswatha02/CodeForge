package com.CodeForge.CodeForge.dto;

import com.CodeForge.CodeForge.model.TestCase;

public class TestCaseDTO {
    private Long id;
    private String inputData;
    private String expectedOutput;
    private Boolean isSample;

    public TestCaseDTO(TestCase testCase) {
        this.id = testCase.getId();
        this.inputData = testCase.getInputData();
        this.expectedOutput = testCase.getExpectedOutput();
        this.isSample = testCase.getIsSample();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getInputData() { return inputData; }
    public void setInputData(String inputData) { this.inputData = inputData; }

    public String getExpectedOutput() { return expectedOutput; }
    public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }

    public Boolean getIsSample() { return isSample; }
    public void setIsSample(Boolean isSample) { this.isSample = isSample; }
}