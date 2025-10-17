package com.CodeForge.CodeForge.dto;

import com.CodeForge.CodeForge.model.Problem;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Set;

public class ProblemRequest {

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Slug is required")
    private String slug;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Input format is required")
    private String inputFormat;

    @NotBlank(message = "Output format is required")
    private String outputFormat;

    @NotNull(message = "Time limit is required")
    private Integer timeLimitMs = 2000;

    @NotNull(message = "Memory limit is required")
    private Integer memoryLimitMb = 256;

    @NotNull(message = "Difficulty is required")
    private Problem.Difficulty difficulty;

    private Set<Long> categoryIds;

    @NotNull(message = "At least one test case is required")
    private List<TestCaseRequest> testCases;

    @NotNull(message = "At least one code template is required")
    private List<CodeTemplateRequest> codeTemplates;

    // Manual getters since Lombok is not working
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getInputFormat() {
        return inputFormat;
    }

    public void setInputFormat(String inputFormat) {
        this.inputFormat = inputFormat;
    }

    public String getOutputFormat() {
        return outputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public Integer getTimeLimitMs() {
        return timeLimitMs;
    }

    public void setTimeLimitMs(Integer timeLimitMs) {
        this.timeLimitMs = timeLimitMs;
    }

    public Integer getMemoryLimitMb() {
        return memoryLimitMb;
    }

    public void setMemoryLimitMb(Integer memoryLimitMb) {
        this.memoryLimitMb = memoryLimitMb;
    }

    public Problem.Difficulty getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(Problem.Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public Set<Long> getCategoryIds() {
        return categoryIds;
    }

    public void setCategoryIds(Set<Long> categoryIds) {
        this.categoryIds = categoryIds;
    }

    public List<TestCaseRequest> getTestCases() {
        return testCases;
    }

    public void setTestCases(List<TestCaseRequest> testCases) {
        this.testCases = testCases;
    }

    public List<CodeTemplateRequest> getCodeTemplates() {
        return codeTemplates;
    }

    public void setCodeTemplates(List<CodeTemplateRequest> codeTemplates) {
        this.codeTemplates = codeTemplates;
    }
}
