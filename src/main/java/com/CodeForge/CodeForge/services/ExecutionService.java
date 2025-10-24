package com.CodeForge.CodeForge.services;

import com.CodeForge.CodeForge.dto.ExecutionRequest;
import com.CodeForge.CodeForge.dto.ExecutionResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ExecutionService {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${execution.service.url:http://localhost:8081}")
    private String executionServiceUrl;

    /**
     * Execute code against test cases using external execution service
     */
    public ExecutionResponse executeCode(ExecutionRequest request) {
        try {
            String url = executionServiceUrl + "/api/execute";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<ExecutionRequest> entity = new HttpEntity<>(request, headers);

            ResponseEntity<ExecutionResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                ExecutionResponse.class
            );

            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                // Handle error response
                ExecutionResponse errorResponse = new ExecutionResponse();
                errorResponse.setStatus("EXECUTION_SERVICE_ERROR");
                errorResponse.setCompilationError("Execution service returned status: " + response.getStatusCode());
                return errorResponse;
            }

        } catch (Exception e) {
            // Handle connection errors, timeouts, etc.
            ExecutionResponse errorResponse = new ExecutionResponse();
            errorResponse.setStatus("EXECUTION_SERVICE_UNAVAILABLE");
            errorResponse.setCompilationError("Failed to connect to execution service: " + e.getMessage());
            return errorResponse;
        }
    }

    /**
     * Check if execution service is available
     */
    public boolean isExecutionServiceAvailable() {
        try {
            String url = executionServiceUrl + "/api/health";
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            return false;
        }
    }
}
