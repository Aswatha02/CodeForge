package com.CodeForge.CodeForge.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.CodeForge.CodeForge.dto.ExecutionRequest;
import com.CodeForge.CodeForge.dto.ExecutionResponse;
import com.CodeForge.CodeForge.services.ExecutionService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ExecutionController {

    @Autowired
    private ExecutionService executionService;

    @PostMapping("/execute")
    public ResponseEntity<ExecutionResponse> executeCode(@RequestBody ExecutionRequest request) {
        try {
            System.out.println("Received execution request for language: " + request.getLanguage());
            System.out.println("Code length: " + (request.getCombinedCode() != null ? request.getCombinedCode().length() : 0));
            System.out.println("Test cases: " + (request.getTestCases() != null ? request.getTestCases().size() : 0));
            
            ExecutionResponse response = executionService.executeCode(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            System.err.println("Execution error: " + e.getMessage());
            e.printStackTrace();
            
            ExecutionResponse errorResponse = new ExecutionResponse();
            errorResponse.setStatus("INTERNAL_ERROR");
            errorResponse.setCompilationError("Internal server error: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @GetMapping("/execute/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Execution service is running");
    }
}