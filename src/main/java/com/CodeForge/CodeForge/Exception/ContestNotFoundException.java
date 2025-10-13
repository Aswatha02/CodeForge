package com.CodeForge.CodeForge.Exception;

public class ContestNotFoundException extends RuntimeException {
    public ContestNotFoundException(Long contestId) {
        super("Contest not found with id: " + contestId);
    }
}
