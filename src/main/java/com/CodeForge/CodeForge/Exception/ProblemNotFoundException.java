package com.CodeForge.CodeForge.Exception;

public class ProblemNotFoundException extends RuntimeException {
    public ProblemNotFoundException(Long problemId) {
        super("Problem not found with id: " + problemId);
    }
}
