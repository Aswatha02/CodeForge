package com.CodeForge.CodeForge.dto;


import com.CodeForge.CodeForge.model.Submission;

public class SubmissionRequest {
    private String code;
    private String language; // Accept as string from JSON

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getLanguage() { return language; }
    public void setLanguage(String language) { this.language = language; }

    // Convert string to enum
    public Submission.Language getLanguageEnum() {
        if (language == null) return null;
        try {
            return Submission.Language.valueOf(language.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid language: " + language);
        }
    }
}
