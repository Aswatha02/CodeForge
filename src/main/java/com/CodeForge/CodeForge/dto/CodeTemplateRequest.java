package com.CodeForge.CodeForge.dto;

import jakarta.validation.constraints.NotBlank;

public class CodeTemplateRequest {

    @NotBlank(message = "Language is required")
    private String language;

    @NotBlank(message = "Template code is required")
    private String templateCode;

    // Manual getters since Lombok is not working
    public String getTemplate() {
        return templateCode;
    }

    public void setTemplate(String template) {
        this.templateCode = template;
    }

    // Manual getters since Lombok is not working
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }
}
