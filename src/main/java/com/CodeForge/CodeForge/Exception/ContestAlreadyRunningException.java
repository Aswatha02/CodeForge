package com.CodeForge.CodeForge.Exception;

public class ContestAlreadyRunningException extends RuntimeException {
    public ContestAlreadyRunningException(Long contestId) {
        super("Contest with id " + contestId + " is already running and cannot be modified");
    }
}
