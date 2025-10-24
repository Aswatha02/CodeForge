package com.CodeForge.CodeForge.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Entity
@Table(name = "problems")
public class Problem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title is required")
    @Size(min = 5, max = 255, message = "Title must be between 5 and 255 characters")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Slug is required")
    @Size(min = 3, max = 100, message = "Slug must be between 3 and 100 characters")
    @Column(unique = true, nullable = false)
    private String slug;

    @NotBlank(message = "Description is required")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    // Input format specification for users
    @NotBlank(message = "Input format is required")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String inputFormat;

    // Output format specification for users
    @NotBlank(message = "Output format is required")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String outputFormat;

    // Constraints and limits
    @Column(name = "time_limit_ms", nullable = false)
    private Integer timeLimitMs = 2000; // milliseconds

    @Column(name = "memory_limit_mb", nullable = false)
    private Integer memoryLimitMb = 256; // MB

    // Additional constraints and metadata
    @Column(columnDefinition = "TEXT")
    private String constraints; // Additional problem constraints

    @Column(nullable = true)
    private Integer points = 100; // Points awarded for solving

    @Column(columnDefinition = "TEXT")
    private String tags; // Comma-separated tags for categorization

    // Example input and output for display
    @Column(columnDefinition = "TEXT")
    private String exampleInput;

    @Column(columnDefinition = "TEXT")
    private String exampleOutput;

    // Function signature for the problem
    @NotBlank(message = "Function name is required")
    @Column(name = "function_name", nullable = false)
    private String functionName = "solve";

    // Parameters as JSON string: [{"name": "nums", "type": "int[]"}, {"name": "target", "type": "int"}]
    @NotBlank(message = "Parameters are required")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String parameters = "[{\"name\": \"input\", \"type\": \"any\"}]";

    @NotBlank(message = "Return type is required")
    @Column(name = "return_type", nullable = false)
    private String returnType = "any";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty = Difficulty.EASY;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    @Column(nullable = false)
    private boolean isPrivate = false;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "problem_categories",
        joinColumns = @JoinColumn(name = "problem_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    @OneToMany(mappedBy = "problem", cascade = {CascadeType.MERGE, CascadeType.REMOVE}, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<CodeTemplate> codeTemplates = new ArrayList<>();

    @OneToMany(mappedBy = "problem", cascade = {CascadeType.MERGE, CascadeType.REMOVE}, fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<TestCase> testCases = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "problem",cascade = CascadeType.ALL ,orphanRemoval = true,fetch = FetchType.LAZY)
    private List<Submission> submissions = new ArrayList<>();


    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum Difficulty {
        EASY, MEDIUM, HARD
    }

    public enum Status {
        ACTIVE, INACTIVE, DRAFT
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Helper methods for code templates
    public void addCodeTemplate(CodeTemplate codeTemplate) {
        codeTemplates.add(codeTemplate);
        codeTemplate.setProblem(this);
    }

    public void removeCodeTemplate(CodeTemplate codeTemplate) {
        codeTemplates.remove(codeTemplate);
        codeTemplate.setProblem(null);
    }

    // Complete Getters
    public Long getId() { 
        return this.id; 
    }

    public String getTitle() { 
        return this.title; 
    }

    public String getSlug() { 
        return this.slug; 
    }

    public String getDescription() { 
        return this.description; 
    }

    public String getInputFormat() { 
        return this.inputFormat; 
    }

    public String getOutputFormat() { 
        return this.outputFormat; 
    }

    public Integer getTimeLimitMs() { 
        return this.timeLimitMs; 
    }

    public Integer getMemoryLimitMb() { 
        return this.memoryLimitMb; 
    }

    public String getFunctionName() { 
        return this.functionName; 
    }

    public String getParameters() { 
        return this.parameters; 
    }

    public String getReturnType() { 
        return this.returnType; 
    }

    public Difficulty getDifficulty() { 
        return this.difficulty; 
    }

    public User getCreator() { 
        return this.creator; 
    }

    public Set<Category> getCategories() { 
        return this.categories; 
    }

    public List<CodeTemplate> getCodeTemplates() { 
        return this.codeTemplates; 
    }

    public List<TestCase> getTestCases() { 
        return this.testCases; 
    }

    public List<Submission> getSubmissions() { 
        return this.submissions; 
    }

    public LocalDateTime getCreatedAt() { 
        return this.createdAt; 
    }

    public LocalDateTime getUpdatedAt() { 
        return this.updatedAt; 
    }

    public Status getStatus() {
        return this.status;
    }

    public String getConstraints() {
        return this.constraints;
    }

    public Integer getPoints() {
        return this.points;
    }

    public String getTags() {
        return this.tags;
    }

    public String getExampleInput() {
        return this.exampleInput;
    }

    public String getExampleOutput() {
        return this.exampleOutput;
    }

    public Boolean getIsPrivate() {
        return this.isPrivate;
    }

    // Complete Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setInputFormat(String inputFormat) {
        this.inputFormat = inputFormat;
    }

    public void setOutputFormat(String outputFormat) {
        this.outputFormat = outputFormat;
    }

    public void setTimeLimitMs(Integer timeLimitMs) {
        this.timeLimitMs = timeLimitMs;
    }

    public void setMemoryLimitMb(Integer memoryLimitMb) {
        this.memoryLimitMb = memoryLimitMb;
    }

    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;
    }

    public void setReturnType(String returnType) {
        this.returnType = returnType;
    }

    public void setDifficulty(Difficulty difficulty) {
        this.difficulty = difficulty;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public void setCategories(Set<Category> categories) {
        this.categories = categories;
    }

    public void setCodeTemplates(List<CodeTemplate> codeTemplates) {
        this.codeTemplates = codeTemplates;
    }

    public void setTestCases(List<TestCase> testCases) {
        this.testCases = testCases;
    }

    public void setSubmissions(List<Submission> submissions) {
        this.submissions = submissions;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setConstraints(String constraints) {
        this.constraints = constraints;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public void setTags(String tags) {
        this.tags = tags;
    }

    public void setExampleInput(String exampleInput) {
        this.exampleInput = exampleInput;
    }

    public void setExampleOutput(String exampleOutput) {
        this.exampleOutput = exampleOutput;
    }

    public void setIsPrivate(Boolean isPrivate) {
        this.isPrivate = isPrivate;
    }
}
