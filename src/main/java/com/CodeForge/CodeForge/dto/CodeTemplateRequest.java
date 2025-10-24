package com.CodeForge.CodeForge.dto;

import jakarta.validation.constraints.NotBlank;

public class CodeTemplateRequest {

    @NotBlank(message = "Language is required")
    private String language;

    @NotBlank(message = "Visible code is required")
    private String visibleCode;

    // Hidden code for test execution
    private String hiddenCode;

    // Manual getters since Lombok is not working
    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getVisibleCode() {
        return visibleCode;
    }

    public void setVisibleCode(String visibleCode) {
        this.visibleCode = visibleCode;
    }

    public String getHiddenCode() {
        return hiddenCode;
    }

    public void setHiddenCode(String hiddenCode) {
        this.hiddenCode = hiddenCode;
    }

    // Backward compatibility getters
    public String getTemplateCode() {
        return visibleCode;
    }

    public void setTemplateCode(String templateCode) {
        this.visibleCode = templateCode;
    }

    public String getWrapperCode() {
        return hiddenCode;
    }

    public void setWrapperCode(String wrapperCode) {
        this.hiddenCode = wrapperCode;
    }
}
