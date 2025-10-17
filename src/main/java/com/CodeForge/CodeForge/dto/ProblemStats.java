package com.CodeForge.CodeForge.dto;

import lombok.Data;

@Data
public class ProblemStats {
    private Long problemId;
    private Integer testCaseCount;
    private String difficulty;
}