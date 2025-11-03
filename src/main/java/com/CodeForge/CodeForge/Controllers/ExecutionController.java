package com.CodeForge.CodeForge.Controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.CodeForge.CodeForge.dto.ExecutionRequest;
import com.CodeForge.CodeForge.dto.ExecutionResponse;
import com.CodeForge.CodeForge.services.ExecutionService;

@RestController
@RequestMapping("/api")
public class ExecutionController {

    @Autowired
    private ExecutionService executionService;

    @PostMapping("/execute")
    public ResponseEntity<ExecutionResponse> executeCode(@RequestBody ExecutionRequest request) {
        try {
            ExecutionResponse response = executionService.executeCode(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ExecutionResponse errorResponse = new ExecutionResponse();
            errorResponse.setStatus("INTERNAL_ERROR");
            errorResponse.setCompilationError("Internal server error: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
