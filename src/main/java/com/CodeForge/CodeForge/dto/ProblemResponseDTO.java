package com.CodeForge.CodeForge.dto;

import com.CodeForge.CodeForge.model.Problem;
import com.CodeForge.CodeForge.model.CodeTemplate;
import com.CodeForge.CodeForge.model.Category;
import com.CodeForge.CodeForge.model.TestCase;
import com.CodeForge.CodeForge.model.User;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.Collectors;
import java.util.Map;

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

    
    // ADDED: Function signature fields
    private String functionName;
    private List<Map<String, String>> parameters;
    private String returnType;

    public ProblemResponseDTO(Problem problem) {
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
        
        // ADDED: Function signature mapping
        this.functionName = problem.getFunctionName();
        this.returnType = problem.getReturnType();
        
        // Parse parameters JSON string to List<Map>
        try {
            ObjectMapper mapper = new ObjectMapper();
            if (problem.getParameters() != null && !problem.getParameters().trim().isEmpty()) {
                this.parameters = mapper.readValue(problem.getParameters(), 
                    new TypeReference<List<Map<String, String>>>() {});
            } else {
                // Default parameter if none provided
                this.parameters = new ArrayList<>();
                Map<String, String> defaultParam = new HashMap<>();
                defaultParam.put("name", "input");
                defaultParam.put("type", "any");
                this.parameters.add(defaultParam);
            }
        } catch (Exception e) {
            // Fallback to default parameters if parsing fails
            this.parameters = new ArrayList<>();
            Map<String, String> defaultParam = new HashMap<>();
            defaultParam.put("name", "input");
            defaultParam.put("type", "any");
            this.parameters.add(defaultParam);
        }
        
        // Convert creator to DTO
        if (problem.getCreator() != null) {
            this.creator = new UserDTO(problem.getCreator());
        }
        
        // Convert categories to DTOs
        if (problem.getCategories() != null) {
            this.categories = problem.getCategories().stream()
                    .map(CategoryDTO::new)
                    .collect(Collectors.toList());
        }
        
        // Convert code templates to DTOs
        if (problem.getCodeTemplates() != null) {
            this.codeTemplates = problem.getCodeTemplates().stream()
                    .map(CodeTemplateDTO::new)
                    .collect(Collectors.toList());
        }
        
        // Convert test cases to DTOs
        if (problem.getTestCases() != null) {
            this.testCases = problem.getTestCases().stream()
                    .map(TestCaseDTO::new)
                    .collect(Collectors.toList());
        }
    }

    // Getters and Setters for existing fields
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
    public void setCodeTemplates(List<CodeTemplateDTO> codeTemplates) { this.codeTemplates = codeTemplates; }

    public List<TestCaseDTO> getTestCases() { return testCases; }
    public void setTestCases(List<TestCaseDTO> testCases) { this.testCases = testCases; }

    // ADDED: Getters and Setters for function signature fields
    public String getFunctionName() { return functionName; }
    public void setFunctionName(String functionName) { this.functionName = functionName; }

    public List<Map<String, String>> getParameters() { return parameters; }
    public void setParameters(List<Map<String, String>> parameters) { this.parameters = parameters; }

    public String getReturnType() { return returnType; }
    public void setReturnType(String returnType) { this.returnType = returnType; }
}