package com.CodeForge.CodeForge.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class TestCaseRequest {

    @NotBlank(message = "Input data is required")
    private String inputData; // JSON format

    @NotBlank(message = "Expected output is required")
    private String expectedOutput;

    @NotNull(message = "Is sample flag is required")
    private Boolean isSample = true;

    // NEW: Visibility flag - true for public (examples), false for hidden (judging only)
    @NotNull(message = "Is public flag is required")
    private Boolean isPublic = true;

    private String explanation;

    private String testCaseName;

    // Manual getters since Lombok is not working
    public String getInputData() {
        return inputData;
    }

    public void setInputData(String inputData) {
        this.inputData = inputData;
    }

    public String getExpectedOutput() {
        return expectedOutput;
    }

    public void setExpectedOutput(String expectedOutput) {
        this.expectedOutput = expectedOutput;
    }

    public Boolean getIsSample() {
        return isSample;
    }

    public void setIsSample(Boolean isSample) {
        this.isSample = isSample;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getExplanation() {
        return explanation;
    }

    public void setExplanation(String explanation) {
        this.explanation = explanation;
    }

    public String getTestCaseName() {
        return testCaseName;
    }

    public void setTestCaseName(String testCaseName) {
        this.testCaseName = testCaseName;
    }
}
