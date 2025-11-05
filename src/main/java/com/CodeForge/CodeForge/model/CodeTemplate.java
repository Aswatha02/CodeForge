package com.CodeForge.CodeForge.model;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "code_templates")
public class CodeTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language;

    @Column(name = "template_code", columnDefinition = "TEXT")
    private String templateCode;

    @Column(name = "visible_code", columnDefinition = "TEXT")
    private String visibleCode;

    @Column(name = "hidden_code", columnDefinition = "TEXT")
    private String hiddenCode;

    @JsonBackReference
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public enum Language {
        JAVA, PYTHON, JAVASCRIPT, CPP, C
    }

    // Constructors
    public CodeTemplate() {}

    public CodeTemplate(Language language, String templateCode, Problem problem) {
        this.language = language;
        this.templateCode = templateCode;
        this.problem = problem;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Language getLanguage() { return language; }
    public void setLanguage(Language language) { this.language = language; }

    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }

    public String getVisibleCode() { return visibleCode; }
    public void setVisibleCode(String visibleCode) { this.visibleCode = visibleCode; }

    public String getHiddenCode() { return hiddenCode; }
    public void setHiddenCode(String hiddenCode) { this.hiddenCode = hiddenCode; }

    public Problem getProblem() { return problem; }
    public void setProblem(Problem problem) { this.problem = problem; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
