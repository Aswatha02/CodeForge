package com.CodeForge.CodeForge.dto;

import java.util.List;
import java.util.Map;

public class ContestProblemResponse {
    private Long id;
    private String title;
    private String difficulty;
    private Integer points;
    private Integer totalSubmissions;
    private Integer acceptedSubmissions;
    private Boolean solved; // Has current user solved it?
    private Integer userAttempts;
    private List<CategoryDTO> categories;
    
    // Code editor fields
    private String description;
    private String functionName;
    private String parameters;
    private String returnType;
    private Map<String, CodeTemplate> codeTemplates;
    private List<TestCaseDTO> testCases;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    
    public Integer getTotalSubmissions() { return totalSubmissions; }
    public void setTotalSubmissions(Integer totalSubmissions) { this.totalSubmissions = totalSubmissions; }
    
    public Integer getAcceptedSubmissions() { return acceptedSubmissions; }
    public void setAcceptedSubmissions(Integer acceptedSubmissions) { this.acceptedSubmissions = acceptedSubmissions; }
    
    public Boolean getSolved() { return solved; }
    public void setSolved(Boolean solved) { this.solved = solved; }
    
    public Integer getUserAttempts() { return userAttempts; }
    public void setUserAttempts(Integer userAttempts) { this.userAttempts = userAttempts; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getFunctionName() { return functionName; }
    public void setFunctionName(String functionName) { this.functionName = functionName; }
    
    public String getParameters() { return parameters; }
    public void setParameters(String parameters) { this.parameters = parameters; }
    
    public String getReturnType() { return returnType; }
    public void setReturnType(String returnType) { this.returnType = returnType; }
    
    public Map<String, CodeTemplate> getCodeTemplates() { return codeTemplates; }
    public void setCodeTemplates(Map<String, CodeTemplate> codeTemplates) { this.codeTemplates = codeTemplates; }
    
    public List<TestCaseDTO> getTestCases() { return testCases; }
    public void setTestCases(List<TestCaseDTO> testCases) { this.testCases = testCases; }
    
    public List<CategoryDTO> getCategories() { return categories; }
    public void setCategories(List<CategoryDTO> categories) { this.categories = categories; }
    
    // Inner class for code templates
    public static class CodeTemplate {
        private String visibleCode;
        private String hiddenCode;
        
        public CodeTemplate() {}
        
        public CodeTemplate(String visibleCode, String hiddenCode) {
            this.visibleCode = visibleCode;
            this.hiddenCode = hiddenCode;
        }
        
        public String getVisibleCode() { return visibleCode; }
        public void setVisibleCode(String visibleCode) { this.visibleCode = visibleCode; }
        
        public String getHiddenCode() { return hiddenCode; }
        public void setHiddenCode(String hiddenCode) { this.hiddenCode = hiddenCode; }
    }
    
    // Inner class for test cases
    public static class TestCaseDTO {
        private Long id;
        private String inputData;
        private String expectedOutput;
        private Boolean isSample;
        private Integer weight;
        
        public TestCaseDTO() {}
        
        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        
        public String getInputData() { return inputData; }
        public void setInputData(String inputData) { this.inputData = inputData; }
        
        public String getExpectedOutput() { return expectedOutput; }
        public void setExpectedOutput(String expectedOutput) { this.expectedOutput = expectedOutput; }
        
        public Boolean getIsSample() { return isSample; }
        public void setIsSample(Boolean isSample) { this.isSample = isSample; }
        
        public Integer getWeight() { return weight; }
        public void setWeight(Integer weight) { this.weight = weight; }
    }
}
