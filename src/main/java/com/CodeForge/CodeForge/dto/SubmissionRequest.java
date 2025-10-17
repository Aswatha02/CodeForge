package com.CodeForge.CodeForge.dto;


import com.CodeForge.CodeForge.model.Submission;

public class SubmissionRequest {
    private String code;
    private Submission.Language language; // Use the enum type

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public Submission.Language getLanguage() { return language; }
    public void setLanguage(Submission.Language language) { this.language = language; }
}