package com.CodeForge.CodeForge.Exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

@ControllerAdvice
public class GlobalExceptionHandler {

    // Specific handler for ContestNotFoundException (keep this)
    @ExceptionHandler(ContestNotFoundException.class)
    public ResponseEntity<String> handleContestNotFound(ContestNotFoundException ex, WebRequest request) {
        return new ResponseEntity<>("Contest not found: " + ex.getMessage(), HttpStatus.NOT_FOUND);
    }

    // General handler for other RuntimeExceptions (exclude ContestNotFound to avoid ambiguity)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException ex, WebRequest request) {
        if (ex instanceof ContestNotFoundException) {
            // Skip if it's ContestNotFound (handled above)
            throw ex;  // Re-throw to let specific handler catch it
        }
        return new ResponseEntity<>("Runtime error: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // General handler for other exceptions
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGlobalException(Exception ex, WebRequest request) {
        return new ResponseEntity<>("An error occurred: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}