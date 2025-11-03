package com.CodeForge.CodeForge.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

import com.CodeForge.CodeForge.dto.ExecutionRequest;
import com.CodeForge.CodeForge.dto.ExecutionResponse;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class ExecutionService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Execute code against test cases using local Docker execution
     */
    public ExecutionResponse executeCode(ExecutionRequest request) {
        ExecutionResponse response = new ExecutionResponse();
        List<ExecutionResponse.TestCaseExecutionResult> results = new ArrayList<>();

        try {
            // Process each test case
            for (ExecutionRequest.TestCaseExecution testCase : request.getTestCases()) {
                ExecutionResult result = executeSingleTestCase(
                    request.getCombinedCode(),
                    request.getLanguage(),
                    testCase.getInputData(),
                    request.getTimeLimitMs(),
                    request.getMemoryLimitMb()
                );

                ExecutionResponse.TestCaseExecutionResult resultDto = new ExecutionResponse.TestCaseExecutionResult();
                resultDto.setTestCaseId(testCase.getTestCaseId());

                if (result.timedOut) {
                    resultDto.setStatus("TIME_LIMIT_EXCEEDED");
                    resultDto.setErrorMessage("Time limit exceeded");
                } else if (result.error != null) {
                    if (result.error.contains("Compilation") || result.error.contains("syntax")) {
                        response.setStatus("COMPILATION_ERROR");
                        response.setCompilationError(result.error);
                        return response;
                    } else {
                        resultDto.setStatus("RUNTIME_ERROR");
                        resultDto.setErrorMessage(result.error);
                    }
                } else {
                    // Compare outputs
                    String actualOutput = normalizeOutput(result.output);
                    String expectedOutput = normalizeOutput(testCase.getExpectedOutput());

                    if (compareOutputs(actualOutput, expectedOutput)) {
                        resultDto.setStatus("PASSED");
                        resultDto.setActualOutput(actualOutput);
                    } else {
                        resultDto.setStatus("FAILED");
                        resultDto.setActualOutput(actualOutput);
                        // Note: expectedOutput is not set in TestCaseExecutionResult
                    }
                }

                results.add(resultDto);
            }

            // Set overall response
            response.setStatus("SUCCESS");
            response.setTestCaseResults(results);
            response.setTotalExecutionTime(calculateTotalTime(results));
            response.setMaxMemoryUsed(calculateMaxMemory(results));

        } catch (Exception e) {
            response.setStatus("EXECUTION_SERVICE_ERROR");
            response.setCompilationError("Internal execution error: " + e.getMessage());
        }

        return response;
    }

    private ExecutionResult executeSingleTestCase(String code, String language,
                                               String inputJson, int timeLimitMs, int memoryLimitMb) throws Exception {

        // Create execution code based on language
        String executionCode = createExecutionCode(code, language, inputJson);

        return executeInDocker(executionCode, language, timeLimitMs, memoryLimitMb);
    }

    private String createExecutionCode(String userCode, String language, String inputJson) {
        try {
            JsonNode inputNode = objectMapper.readTree(inputJson);

            return switch (language.toUpperCase()) {
                case "JAVASCRIPT" -> createJavaScriptExecutionCode(userCode, inputNode);
                case "PYTHON" -> createPythonExecutionCode(userCode, inputNode);
                case "JAVA" -> createJavaExecutionCode(userCode, inputNode);
                case "CPP" -> createCppExecutionCode(userCode, inputNode);
                case "C" -> createCExecutionCode(userCode, inputNode);
                default -> throw new IllegalArgumentException("Unsupported language: " + language);
            };
        } catch (Exception e) {
            throw new RuntimeException("Failed to create execution code: " + e.getMessage(), e);
        }
    }

    private String createJavaScriptExecutionCode(String userCode, JsonNode inputNode) {
        StringBuilder sb = new StringBuilder();
        sb.append(userCode).append("\n\n");
        sb.append("// Test execution\n");

        // Extract individual parameters from JSON and create variables
        List<String> paramNames = new ArrayList<>();
        inputNode.fieldNames().forEachRemaining(fieldName -> {
            JsonNode fieldValue = inputNode.get(fieldName);
            sb.append("const ").append(fieldName).append(" = ");

            if (fieldValue.isTextual()) {
                sb.append("\"").append(fieldValue.asText()).append("\"");
            } else {
                sb.append(fieldValue.toString());
            }
            sb.append(";\n");
            paramNames.add(fieldName);
        });

        // Assume function name is 'solve' for now
        sb.append("const result = solve(");
        sb.append(String.join(", ", paramNames));
        sb.append(");\n");

        sb.append("console.log(JSON.stringify(result));");
        return sb.toString();
    }

    private String createPythonExecutionCode(String userCode, JsonNode inputNode) {
        StringBuilder sb = new StringBuilder();
        sb.append(userCode).append("\n\n");
        sb.append("# Test execution\n");

        List<String> paramNames = new ArrayList<>();
        inputNode.fieldNames().forEachRemaining(fieldName -> {
            JsonNode fieldValue = inputNode.get(fieldName);
            sb.append(fieldName).append(" = ");

            if (fieldValue.isTextual()) {
                sb.append("\"").append(fieldValue.asText()).append("\"");
            } else {
                sb.append(fieldValue.toString());
            }
            sb.append("\n");
            paramNames.add(fieldName);
        });

        // Assume function name is 'solve'
        sb.append("result = solve(");
        sb.append(String.join(", ", paramNames));
        sb.append(")\n");

        sb.append("import json\n");
        sb.append("print(json.dumps(result))");
        return sb.toString();
    }

    private String createJavaExecutionCode(String userCode, JsonNode inputNode) {
        StringBuilder sb = new StringBuilder();
        sb.append("import java.util.*;\n\n");

        // Add user's code (which should contain the Solution class)
        sb.append(userCode).append("\n\n");

        sb.append("public class Main {\n");
        sb.append("    public static void main(String[] args) {\n");

        // Create variables from input JSON
        List<String> paramNames = new ArrayList<>();
        inputNode.fieldNames().forEachRemaining(fieldName -> {
            JsonNode fieldValue = inputNode.get(fieldName);

            if (fieldValue.isArray()) {
                sb.append("        int[] ").append(fieldName).append(" = new int[]{");
                for (int i = 0; i < fieldValue.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(fieldValue.get(i).asInt());
                }
                sb.append("};\n");
                paramNames.add(fieldName);
            } else if (fieldValue.isNumber()) {
                sb.append("        int ").append(fieldName).append(" = ").append(fieldValue.asInt()).append(";\n");
                paramNames.add(fieldName);
            } else if (fieldValue.isTextual()) {
                sb.append("        String ").append(fieldName).append(" = \"").append(fieldValue.asText()).append("\";\n");
                paramNames.add(fieldName);
            }
        });

        sb.append("\n");

        // Call user's function
        sb.append("        // Call user's solution\n");
        sb.append("        Solution solution = new Solution();\n");
        sb.append("        int[] result = solution.twoSum(");
        sb.append(String.join(", ", paramNames));
        sb.append(");\n\n");

        // Output the result as JSON array
        sb.append("        // Output result as JSON array\n");
        sb.append("        System.out.print(\"[\");\n");
        sb.append("        for (int i = 0; i < result.length; i++) {\n");
        sb.append("            if (i > 0) System.out.print(\",\");\n");
        sb.append("            System.out.print(result[i]);\n");
        sb.append("        }\n");
        sb.append("        System.out.println(\"]\");\n");
        sb.append("    }\n");
        sb.append("}");
        return sb.toString();
    }

    private String createCppExecutionCode(String userCode, JsonNode inputNode) {
        StringBuilder sb = new StringBuilder();

        // Add necessary includes
        sb.append("#include <iostream>\n");
        sb.append("#include <vector>\n");
        sb.append("#include <string>\n\n");

        // Add user's code
        sb.append(userCode).append("\n\n");

        // Create main function
        sb.append("int main() {\n");

        // Create variables from input JSON
        List<String> paramNames = new ArrayList<>();
        inputNode.fieldNames().forEachRemaining(fieldName -> {
            JsonNode fieldValue = inputNode.get(fieldName);

            if (fieldValue.isArray()) {
                sb.append("    std::vector<int> ").append(fieldName).append(" = {");
                for (int i = 0; i < fieldValue.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(fieldValue.get(i).asInt());
                }
                sb.append("};\n");
                paramNames.add(fieldName);
            } else if (fieldValue.isNumber()) {
                sb.append("    int ").append(fieldName).append(" = ").append(fieldValue.asInt()).append(";\n");
                paramNames.add(fieldName);
            } else if (fieldValue.isTextual()) {
                sb.append("    std::string ").append(fieldName).append(" = \"").append(fieldValue.asText()).append("\";\n");
                paramNames.add(fieldName);
            }
        });

        sb.append("\n");

        // Call user's function
        sb.append("    // Call user's solution\n");
        sb.append("    auto result = solve(");
        sb.append(String.join(", ", paramNames));
        sb.append(");\n\n");

        // Output the result
        sb.append("    // Output result as JSON array\n");
        sb.append("    std::cout << \"[\";\n");
        sb.append("    for (size_t i = 0; i < result.size(); ++i) {\n");
        sb.append("        std::cout << result[i];\n");
        sb.append("        if (i < result.size() - 1) std::cout << \",\";\n");
        sb.append("    }\n");
        sb.append("    std::cout << \"]\" << std::endl;\n");
        sb.append("    return 0;\n");
        sb.append("}");

        return sb.toString();
    }

    private String createCExecutionCode(String userCode, JsonNode inputNode) {
        StringBuilder sb = new StringBuilder();

        // Add necessary includes
        sb.append("#include <stdio.h>\n");
        sb.append("#include <stdlib.h>\n\n");

        // Add user's code
        sb.append(userCode).append("\n\n");

        // Create main function
        sb.append("int main() {\n");

        // Create variables from input JSON
        List<String> paramNames = new ArrayList<>();
        final int[] arraySizeHolder = {0};
        final String[] arrayNameHolder = {null};

        inputNode.fieldNames().forEachRemaining(fieldName -> {
            JsonNode fieldValue = inputNode.get(fieldName);

            if (fieldValue.isArray()) {
                sb.append("    int ").append(fieldName).append("[] = {");
                for (int i = 0; i < fieldValue.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(fieldValue.get(i).asInt());
                }
                sb.append("};\n");
                arraySizeHolder[0] = fieldValue.size();
                arrayNameHolder[0] = fieldName;
                paramNames.add(fieldName);
            } else if (fieldValue.isNumber()) {
                sb.append("    int ").append(fieldName).append(" = ").append(fieldValue.asInt()).append(";\n");
                paramNames.add(fieldName);
            } else if (fieldValue.isTextual()) {
                sb.append("    char* ").append(fieldName).append(" = \"").append(fieldValue.asText()).append("\";\n");
                paramNames.add(fieldName);
            }
        });

        if (arrayNameHolder[0] != null) {
            sb.append("    int ").append(arrayNameHolder[0]).append("_size = ").append(arraySizeHolder[0]).append(";\n");
        }

        sb.append("\n");

        // Call user's function
        sb.append("    // Call user's solution\n");
        sb.append("    int* result = solve(");
        List<String> callParams = new ArrayList<>();
        for (String param : paramNames) {
            callParams.add(param);
            if (param.equals(arrayNameHolder[0])) {
                callParams.add(param + "_size");
            }
        }
        sb.append(String.join(", ", callParams));
        sb.append(");\n\n");

        // Output the result
        sb.append("    // Output result as JSON array\n");
        if (arraySizeHolder[0] > 0) {
            sb.append("    printf(\"[\");\n");
            sb.append("    for (int i = 0; i < ").append(arraySizeHolder[0]).append("; ++i) {\n");
            sb.append("        printf(\"%d\", result[i]);\n");
            sb.append("        if (i < ").append(arraySizeHolder[0]).append(" - 1) printf(\",\");\n");
            sb.append("    }\n");
            sb.append("    printf(\"]\\n\");\n");
        } else {
            sb.append("    printf(\"%d\\n\", *result);\n");
        }
        sb.append("    return 0;\n");
        sb.append("}");

        return sb.toString();
    }

    private ExecutionResult executeInDocker(String executionCode, String language,
                                          int timeLimitMs, int memoryLimitMb) throws Exception {
        String imageName = getDockerImage(language);
        String containerName = "execution-" + System.currentTimeMillis();

        // Create temporary files
        Path codeDir = Files.createTempDirectory("execution");
        Path codeFile = codeDir.resolve(getFileName(language));

        Files.write(codeFile, executionCode.getBytes());

        try {
            // Docker run command with resource limits
            String executionCommand = getExecutionCommand(language);
            ProcessBuilder pb;

            // For complex commands (containing && or ;), use shell execution
            if (executionCommand.contains("&&") || executionCommand.contains(";")) {
                pb = new ProcessBuilder(
                    "docker", "run", "--rm",
                    "--name", containerName,
                    "--memory", memoryLimitMb + "m",
                    "--memory-swap", memoryLimitMb + "m",
                    "--cpus", "0.5",
                    "-v", codeDir.toString() + ":/code:ro",
                    imageName,
                    "/bin/sh", "-c", executionCommand
                );
            } else {
                pb = new ProcessBuilder(
                    "docker", "run", "--rm",
                    "--name", containerName,
                    "--memory", memoryLimitMb + "m",
                    "--memory-swap", memoryLimitMb + "m",
                    "--cpus", "0.5",
                    "-v", codeDir.toString() + ":/code:ro",
                    imageName,
                    executionCommand
                );
            }

            Process process = pb.start();

            // Wait with timeout
            boolean finished = process.waitFor(timeLimitMs + 1000, TimeUnit.MILLISECONDS);

            if (!finished) {
                new ProcessBuilder("docker", "kill", containerName).start().waitFor();
                return new ExecutionResult(null, true, 0, 0, "Time limit exceeded");
            }

            // Read output
            String output = readStream(process.getInputStream());
            String error = readStream(process.getErrorStream());
            int exitCode = process.exitValue();

            if (exitCode != 0) {
                return new ExecutionResult(null, false, 0, 0, "Runtime error: " + error);
            }

            // Calculate actual execution metrics (simulated for now)
            int execTime = Math.min(timeLimitMs, 50 + (int)(Math.random() * 100));
            int memory = Math.min(memoryLimitMb, 10 + (int)(Math.random() * 20));

            return new ExecutionResult(output, false, execTime, memory, null);

        } finally {
            deleteDirectory(codeDir);
        }
    }

    private String simulateExecution(String executionCode, String language) {
        // Simple simulation - in real implementation, this would run in Docker
        // For now, return a mock successful output
        return switch (language.toUpperCase()) {
            case "JAVASCRIPT" -> "{\"result\": 42}";
            case "PYTHON" -> "{\"result\": 42}";
            case "JAVA" -> "{\"result\": 42}";
            case "CPP" -> "[42]";
            case "C" -> "[42]";
            default -> "{\"result\": \"success\"}";
        };
    }

    private String getExecutionCommand(String language) {
        return switch (language.toUpperCase()) {
            case "JAVASCRIPT" -> "node /code/main.js";
            case "PYTHON" -> "python /code/main.py";
            case "JAVA" -> "cd /code && javac Main.java && java Main";
            case "CPP" -> "cd /code && g++ -std=c++11 -o main main.cpp && ./main";
            case "C" -> "cd /code && gcc -o main main.c && ./main";
            default -> throw new IllegalArgumentException("Unsupported language: " + language);
        };
    }

    private String getDockerImage(String language) {
        return switch (language.toUpperCase()) {
            case "JAVASCRIPT" -> "node:18-slim";
            case "JAVA" -> "openjdk:17-slim";
            case "PYTHON" -> "python:3.9-slim";
            case "CPP" -> "gcc:latest";
            case "C" -> "gcc:latest";
            default -> throw new IllegalArgumentException("Unsupported language: " + language);
        };
    }

    private String getFileName(String language) {
        return switch (language.toUpperCase()) {
            case "JAVASCRIPT" -> "main.js";
            case "JAVA" -> "Main.java";
            case "PYTHON" -> "main.py";
            case "CPP" -> "main.cpp";
            case "C" -> "main.c";
            default -> throw new IllegalArgumentException("Unsupported language: " + language);
        };
    }

    private String readStream(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder result = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            result.append(line).append("\n");
        }
        return result.toString().trim();
    }

    private String normalizeOutput(String output) {
        return output.trim().replaceAll("\\r\\n", "\n").replaceAll("\\s+", " ");
    }

    private boolean compareOutputs(String actual, String expected) {
        try {
            // Parse both outputs as JSON for flexible comparison
            JsonNode actualNode = objectMapper.readTree(actual.trim());
            JsonNode expectedNode = objectMapper.readTree(expected.trim());

            return actualNode.equals(expectedNode);

        } catch (Exception e) {
            // If JSON parsing fails, do string comparison (normalized)
            return normalizeOutput(actual).equals(normalizeOutput(expected));
        }
    }

    private void deleteDirectory(Path path) throws IOException {
        Files.walk(path)
            .sorted((a, b) -> -a.compareTo(b))
            .forEach(p -> {
                try { Files.delete(p); }
                catch (IOException e) { /* Ignore */ }
            });
    }

    private Integer calculateTotalTime(List<ExecutionResponse.TestCaseExecutionResult> results) {
        // Simplified calculation - in real implementation, track actual times
        return results.size() * 100; // 100ms per test case
    }

    private Integer calculateMaxMemory(List<ExecutionResponse.TestCaseExecutionResult> results) {
        // Simplified calculation - in real implementation, track actual memory usage
        return 50; // 50MB max
    }

    /**
     * Check if execution service is available (always true for local execution)
     */
    public boolean isExecutionServiceAvailable() {
        return true;
    }

    // Result container
    private static class ExecutionResult {
        String output;
        boolean timedOut;
        int executionTime;
        int memoryUsed;
        String error;

        ExecutionResult(String output, boolean timedOut, int executionTime, int memoryUsed, String error) {
            this.output = output;
            this.timedOut = timedOut;
            this.executionTime = executionTime;
            this.memoryUsed = memoryUsed;
            this.error = error;
        }
    }
}
