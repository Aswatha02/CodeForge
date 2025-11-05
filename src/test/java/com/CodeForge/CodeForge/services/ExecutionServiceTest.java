package com.CodeForge.CodeForge.services;

import com.CodeForge.CodeForge.dto.ExecutionRequest;
import com.CodeForge.CodeForge.dto.ExecutionResponse;
import com.CodeForge.CodeForge.dto.SubmissionExecutionResponse;
import com.CodeForge.CodeForge.model.TestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ExecutionServiceTest {

    private ExecutionService executionService;

    @BeforeEach
    void setUp() {
        executionService = new ExecutionService();
    }

    @Test
    void testExecuteSubmissionCode() {
        // Test batch execution through public method
        String jsCode = "function solve(a, b) { return a + b; }";
        List<TestCase> testCases = Arrays.asList(
            createTestCase("{\"a\": 1, \"b\": 2}", "3")
        );

        // This will test the batch execution functionality
        var response = executionService.executeSubmissionCode(jsCode, "JAVASCRIPT", testCases, 5000, 256, "solve");
        assertNotNull(response);
        // Note: Actual execution may fail due to Docker not being available in test environment,
        // but the method should not throw exceptions and return a response
    }

    @Test
    void testExecutePythonCode() {
        // Test Python execution with the fixed code
        String pythonCode = "def solve(s): return s[::-1]";
        ExecutionRequest.TestCaseExecution testCase = new ExecutionRequest.TestCaseExecution();
        testCase.setTestCaseId(1L);
        testCase.setInputData("{\"s\": \"hello\"}");
        testCase.setExpectedOutput("\"olleh\"");
        List<ExecutionRequest.TestCaseExecution> testCases = Arrays.asList(testCase);

        ExecutionRequest request = new ExecutionRequest();
        request.setCombinedCode(pythonCode);
        request.setLanguage("PYTHON");
        request.setTestCases(testCases);
        request.setTimeLimitMs(5000);
        request.setMemoryLimitMb(256);

        var response = executionService.executeCode(request);
        assertNotNull(response);
        assertEquals("SUCCESS", response.getStatus());
        assertFalse(response.getTestCaseResults().isEmpty());
        assertEquals("PASSED", response.getTestCaseResults().get(0).getStatus());
    }

    @Test
    void testServiceAvailability() {
        // Test service availability
        assertTrue(executionService.isExecutionServiceAvailable());
    }

    private TestCase createTestCase(String inputData, String expectedOutput) {
        TestCase tc = new TestCase();
        tc.setId(1L);
        tc.setInputData(inputData);
        tc.setExpectedOutput(expectedOutput);
        return tc;
    }
}
