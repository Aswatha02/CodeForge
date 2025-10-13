package com.CodeForge.CodeForge.Exception;

public class UserNotAuthorizedException extends RuntimeException {
    public UserNotAuthorizedException(String action) {
        super("You are not authorized to perform this action: " + action);
    }
}
