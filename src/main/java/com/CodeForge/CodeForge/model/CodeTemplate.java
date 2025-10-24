package com.CodeForge.CodeForge.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

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
import jakarta.persistence.UniqueConstraint;


@Entity
@Table(
    name = "code_templates",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"problem_id", "language"})
    }
)
public class CodeTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "problem_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE) // ✅ Correct way to enforce ON DELETE CASCADE
    private Problem problem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Language language;

    @Column(name = "template_code", columnDefinition = "TEXT", nullable = false)
    private String templateCode;

    @Column(name = "hidden_wrapper_code", columnDefinition = "TEXT")
    private String hiddenWrapperCode; // Hidden code used during judging to wrap user code

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Enum for supported languages
    public static enum Language {
    JAVA, PYTHON, CPP, JAVASCRIPT, C
}

    // Constructors
    public CodeTemplate() {}

    public CodeTemplate(Problem problem, Language language, String templateCode) {
        this.problem = problem;
        this.language = language;
        this.templateCode = templateCode;
    }

    public CodeTemplate(Problem problem, Language language, String templateCode, String hiddenWrapperCode) {
        this.problem = problem;
        this.language = language;
        this.templateCode = templateCode;
        this.hiddenWrapperCode = hiddenWrapperCode;
    }

    // Getters & Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Problem getProblem() { return problem; }
    public void setProblem(Problem problem) { this.problem = problem; }

    public Language getLanguage() { return language; }
    public void setLanguage(Language language) { this.language = language; }

    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }

    public String getTemplate() { return templateCode; }
    public void setTemplate(String template) { this.templateCode = template; }

    public String getHiddenWrapperCode() { return hiddenWrapperCode; }
    public void setHiddenWrapperCode(String hiddenWrapperCode) { this.hiddenWrapperCode = hiddenWrapperCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
