package com.CodeForge.CodeForge.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.CodeForge.CodeForge.model.Problem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ProblemResponseDTO {
    private Long id;
    private String title;
    private String slug;
    private String description;
    private String inputFormat;
    private String outputFormat;
    private Integer timeLimitMs;
    private Integer memoryLimitMb;
    private String difficulty;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserDTO creator;
    private List<CategoryDTO> categories;
    private List<CodeTemplateDTO> codeTemplates;
    private List<TestCaseDTO> testCases;

    // Function signature fields
    private String functionName;
    private List<Map<String, String>> parameters;
    private String returnType;

    // Additional fields
    private String constraints;
    private Integer points;
    private String tags;
    private String exampleInput;
    private String exampleOutput;
    private Boolean isPrivate;

    // Edit form compatibility fields
    private List<Long> categoryIds;
    private String parametersJson;
    private String categoryString;

    // ADD: Default constructor for Jackson
    public ProblemResponseDTO() {
        // Initialize collections to avoid null pointers
        this.parameters = new ArrayList<>();
        this.categories = new ArrayList<>();
        this.codeTemplates = new ArrayList<>();
        this.testCases = new ArrayList<>();
    }

    public ProblemResponseDTO(Problem problem) {
        this(); // Call default constructor to initialize collections
        
        this.id = problem.getId();
        this.title = problem.getTitle();
        this.slug = problem.getSlug();
        this.description = problem.getDescription();
        this.inputFormat = problem.getInputFormat();
        this.outputFormat = problem.getOutputFormat();
        this.timeLimitMs = problem.getTimeLimitMs();
        this.memoryLimitMb = problem.getMemoryLimitMb();
        this.difficulty = problem.getDifficulty().name();
        this.status = problem.getStatus().name();
        this.createdAt = problem.getCreatedAt();
        this.updatedAt = problem.getUpdatedAt();

        // Additional fields mapping
        this.constraints = problem.getConstraints();
        this.points = problem.getPoints();
        this.tags = problem.getTags();
        this.exampleInput = problem.getExampleInput();
        this.exampleOutput = problem.getExampleOutput();
        this.isPrivate = problem.getIsPrivate();

        // Function signature mapping
        this.functionName = problem.getFunctionName();
        this.returnType = problem.getReturnType();

        // Enhanced parameters parsing
        parseParametersSafely(problem.getParameters());
        
        // Convert creator to DTO
        if (problem.getCreator() != null) {
            this.creator = new UserDTO(problem.getCreator());
        }
        
        // Null-safe collection mapping
        this.categories = problem.getCategories() != null ?
            problem.getCategories().stream()
                .map(CategoryDTO::new)
                .collect(Collectors.toList()) : new ArrayList<>();

        this.codeTemplates = problem.getCodeTemplates() != null ?
            problem.getCodeTemplates().stream()
                .map(CodeTemplateDTO::new)
                .collect(Collectors.toList()) : new ArrayList<>();

        this.testCases = problem.getTestCases() != null ?
            problem.getTestCases().stream()
                .map(TestCaseDTO::new)
                .collect(Collectors.toList()) : new ArrayList<>();

        // Set edit form compatibility fields
        this.categoryIds = problem.getCategories() != null ?
            problem.getCategories().stream()
                .map(category -> category.getId())
                .collect(Collectors.toList()) : new ArrayList<>();

        this.parametersJson = problem.getParameters();
        this.categoryString = problem.getCategories() != null ?
            problem.getCategories().stream()
                .map(category -> category.getName())
                .collect(Collectors.joining(", ")) : "";
    }

    private void parseParametersSafely(String parametersJson) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            
            if (parametersJson != null && !parametersJson.trim().isEmpty()) {
                String paramsJson = parametersJson.trim();
                this.parameters = mapper.readValue(paramsJson, new TypeReference<List<Map<String, String>>>() {});
            } else {
                this.parameters = createDefaultParameters();
            }
        } catch (Exception e) {
            System.err.println("Failed to parse parameters JSON: " + e.getMessage());
            System.err.println("Raw parameters string: " + parametersJson);
            this.parameters = createDefaultParameters();
        }
    }

    private List<Map<String, String>> createDefaultParameters() {
        List<Map<String, String>> defaultParams = new ArrayList<>();
        Map<String, String> defaultParam = new HashMap<>();
        defaultParam.put("name", "input");
        defaultParam.put("type", "any");
        defaultParams.add(defaultParam);
        return defaultParams;
    }

    // Getters and Setters (keep all your existing ones)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getInputFormat() { return inputFormat; }
    public void setInputFormat(String inputFormat) { this.inputFormat = inputFormat; }

    public String getOutputFormat() { return outputFormat; }
    public void setOutputFormat(String outputFormat) { this.outputFormat = outputFormat; }

    public Integer getTimeLimitMs() { return timeLimitMs; }
    public void setTimeLimitMs(Integer timeLimitMs) { this.timeLimitMs = timeLimitMs; }

    public Integer getMemoryLimitMb() { return memoryLimitMb; }
    public void setMemoryLimitMb(Integer memoryLimitMb) { this.memoryLimitMb = memoryLimitMb; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    public UserDTO getCreator() { return creator; }
    public void setCreator(UserDTO creator) { this.creator = creator; }

    public List<CategoryDTO> getCategories() { return categories; }
    public void setCategories(List<CategoryDTO> categories) { this.categories = categories; }

    public List<CodeTemplateDTO> getCodeTemplates() { return codeTemplates; }
    public void setCodeTemplate(List<CodeTemplateDTO> codeTemplates) { this.codeTemplates = codeTemplates; }

    public List<TestCaseDTO> getTestCases() { return testCases; }
    public void setTestCases(List<TestCaseDTO> testCases) { this.testCases = testCases; }

    public String getFunctionName() { return functionName; }
    public void setFunctionName(String functionName) { this.functionName = functionName; }

    public List<Map<String, String>> getParameters() { return parameters; }
    public void setParameters(List<Map<String, String>> parameters) { this.parameters = parameters; }

    public String getReturnType() { return returnType; }
    public void setReturnType(String returnType) { this.returnType = returnType; }

    public String getConstraints() { return constraints; }
    public void setConstraints(String constraints) { this.constraints = constraints; }

    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }

    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }

    public String getExampleInput() { return exampleInput; }
    public void setExampleInput(String exampleInput) { this.exampleInput = exampleInput; }

    public String getExampleOutput() { return exampleOutput; }
    public void setExampleOutput(String exampleOutput) { this.exampleOutput = exampleOutput; }

    public Boolean getIsPrivate() { return isPrivate; }
    public void setIsPrivate(Boolean isPrivate) { this.isPrivate = isPrivate; }

    // New getters and setters for edit form compatibility fields
    public List<Long> getCategoryIds() { return categoryIds; }
    public void setCategoryIds(List<Long> categoryIds) { this.categoryIds = categoryIds; }

    public String getParametersJson() { return parametersJson; }
    public void setParametersJson(String parametersJson) { this.parametersJson = parametersJson; }

    public String getCategoryString() { return categoryString; }
    public void setCategoryString(String categoryString) { this.categoryString = categoryString; }
}
