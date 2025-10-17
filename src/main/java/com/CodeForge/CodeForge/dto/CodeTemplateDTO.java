package com.CodeForge.CodeForge.dto;

import com.CodeForge.CodeForge.model.CodeTemplate;
import com.CodeForge.CodeForge.model.CodeTemplate.Language;

import java.time.LocalDateTime;

public class CodeTemplateDTO {
    private Long id;
    private Language language;
    private String templateCode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public CodeTemplateDTO(CodeTemplate codeTemplate) {
        this.id = codeTemplate.getId();
        this.language = codeTemplate.getLanguage();
        this.templateCode = codeTemplate.getTemplateCode();
        this.createdAt = codeTemplate.getCreatedAt();
        this.updatedAt = codeTemplate.getUpdatedAt();
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Language getLanguage() { return language; }
    public void setLanguage(Language language) { this.language = language; }

    public String getTemplateCode() { return templateCode; }
    public void setTemplateCode(String templateCode) { this.templateCode = templateCode; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}