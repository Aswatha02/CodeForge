package com.CodeForge.CodeForge.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        Map<String, String> executionFiles = createExecutionCode(code, language, inputJson);

        return executeInDocker(executionFiles, language, timeLimitMs, memoryLimitMb);
    }

    private Map<String, String> createExecutionCode(String userCode, String language, String inputJson) {
        try {
            JsonNode inputNode = objectMapper.readTree(inputJson);

            return switch (language.toUpperCase()) {
                case "JAVASCRIPT" -> Map.of("main.js", createJavaScriptExecutionCode(userCode, inputNode));
                case "PYTHON" -> Map.of("main.py", createPythonExecutionCode(userCode, inputNode));
                case "JAVA" -> createJavaExecutionFiles(userCode, inputNode);
                case "CPP" -> Map.of("main.cpp", createCppExecutionCode(userCode, inputNode));
                case "C" -> Map.of("main.c", createCExecutionCode(userCode, inputNode));
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

        // Modify user's code to make Solution class non-public to avoid file naming conflicts
        String modifiedUserCode = userCode.replaceFirst("public\\s+class\\s+Solution", "class Solution");
        sb.append(modifiedUserCode).append("\n\n");

        // Parse method signature from user code
        MethodSignature methodSig = parseJavaMethodSignature(userCode);

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
        sb.append("        ").append(methodSig.returnType).append(" result = solution.").append(methodSig.methodName).append("(");
        sb.append(String.join(", ", paramNames));
        sb.append(");\n\n");

        // Output the result based on return type
        sb.append("        // Output result\n");
        if (methodSig.returnType.equals("String")) {
            sb.append("        System.out.println(result);\n");
        } else if (methodSig.returnType.equals("int[]")) {
            sb.append("        System.out.print(\"[\");\n");
            sb.append("        for (int i = 0; i < result.length; i++) {\n");
            sb.append("            if (i > 0) System.out.print(\",\");\n");
            sb.append("            System.out.print(result[i]);\n");
            sb.append("        }\n");
            sb.append("        System.out.println(\"]\");\n");
        } else if (methodSig.returnType.equals("int")) {
            sb.append("        System.out.println(result);\n");
        } else {
            // Default to JSON output for other types
            sb.append("        System.out.println(result);\n");
        }
        sb.append("    }\n");
        sb.append("}");
        return sb.toString();
    }

    private static class MethodSignature {
        String methodName;
        String returnType;
        List<String> paramTypes;
        List<String> paramNames;

        MethodSignature(String methodName, String returnType, List<String> paramTypes, List<String> paramNames) {
            this.methodName = methodName;
            this.returnType = returnType;
            this.paramTypes = paramTypes;
            this.paramNames = paramNames;
        }
    }

    private Map<String, String> createJavaExecutionFiles(String userCode, JsonNode inputNode) {
        Map<String, String> files = new HashMap<>();

        // Solution.java - user's code with public class made non-public
        String modifiedUserCode = userCode.replaceAll("(?i)public\\s+(class\\s+Solution)", "$1");
        files.put("Solution.java", modifiedUserCode);

        // Parse method signature from user code
        MethodSignature methodSig = parseJavaMethodSignature(userCode);

        // Main.java - the execution wrapper
        StringBuilder mainSb = new StringBuilder();
        mainSb.append("import java.util.*;\n\n");
        mainSb.append("public class Main {\n");
        mainSb.append("    public static void main(String[] args) {\n");

        // Create variables from input JSON
        List<String> paramNames = new ArrayList<>();
        inputNode.fieldNames().forEachRemaining(fieldName -> {
            JsonNode fieldValue = inputNode.get(fieldName);

            if (fieldValue.isArray()) {
                // Handle int[] arrays
                mainSb.append("        int[] ").append(fieldName).append(" = new int[]{");
                for (int i = 0; i < fieldValue.size(); i++) {
                    if (i > 0) mainSb.append(", ");
                    mainSb.append(fieldValue.get(i).asInt());
                }
                mainSb.append("};\n");
                paramNames.add(fieldName);
            } else if (fieldValue.isNumber()) {
                // Handle numeric values
                mainSb.append("        int ").append(fieldName).append(" = ").append(fieldValue.asInt()).append(";\n");
                paramNames.add(fieldName);
            } else if (fieldValue.isTextual()) {
                // Handle string values
                mainSb.append("        String ").append(fieldName)
                      .append(" = \"").append(fieldValue.asText().replace("\"", "\\\"")).append("\";\n");
                paramNames.add(fieldName);
            } else if (fieldValue.isBoolean()) {
                // Handle booleans
                mainSb.append("        boolean ").append(fieldName)
                      .append(" = ").append(fieldValue.asBoolean()).append(";\n");
                paramNames.add(fieldName);
            }
        });

        mainSb.append("\n");

        // Call user's function
        mainSb.append("        // Call user's solution\n");
        mainSb.append("        Solution solution = new Solution();\n");
        mainSb.append("        ").append(methodSig.returnType).append(" result = solution.")
              .append(methodSig.methodName).append("(")
              .append(String.join(", ", paramNames)).append(");\n\n");

        // Output the result based on return type
        mainSb.append("        // Output result\n");
        if (methodSig.returnType.equals("String")) {
            // Output as JSON string with quotes to match expected format
            mainSb.append("        System.out.println(\"\\\"\" + result + \"\\\"\");\n");
        } else if (methodSig.returnType.equals("int[]")) {
            mainSb.append("        System.out.print(\"[\");\n");
            mainSb.append("        for (int i = 0; i < result.length; i++) {\n");
            mainSb.append("            if (i > 0) System.out.print(\",\");\n");
            mainSb.append("            System.out.print(result[i]);\n");
            mainSb.append("        }\n");
            mainSb.append("        System.out.println(\"]\");\n");
        } else if (methodSig.returnType.equals("int")
                || methodSig.returnType.equals("boolean")
                || methodSig.returnType.equals("double")
                || methodSig.returnType.equals("long")) {
            mainSb.append("        System.out.println(result);\n");
        } else {
            // Default to JSON output for other object types
            mainSb.append("        System.out.println(result);\n");
        }

        mainSb.append("    }\n");
        mainSb.append("}\n");

        files.put("Main.java", mainSb.toString());
        return files;
    }

    private MethodSignature parseJavaMethodSignature(String userCode) {
        System.out.println("=== DEBUG: Parsing Java Method from ExecutionService ===");
        System.out.println("User code: " + userCode.substring(0, Math.min(300, userCode.length())));
        
        // Multiple patterns to catch different method signatures
        Pattern[] patterns = {
            Pattern.compile("public\\s+(\\w+(?:\\[\\])?)\\s+(\\w+)\\s*\\(([^)]*)\\)"),
            Pattern.compile("private\\s+(\\w+(?:\\[\\])?)\\s+(\\w+)\\s*\\(([^)]*)\\)"), 
            Pattern.compile("(\\w+(?:\\[\\])?)\\s+(\\w+)\\s*\\(([^)]*)\\)")
        };
        
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(userCode);
            if (matcher.find()) {
                String returnType = matcher.group(1);
                String methodName = matcher.group(2);
                String params = matcher.group(3).trim();
                
                System.out.println("Found method: " + methodName + " return: " + returnType + " params: " + params);
                
                List<String> paramTypes = new ArrayList<>();
                List<String> paramNames = new ArrayList<>();

                if (!params.isEmpty()) {
                    String[] paramParts = params.split(",");
                    for (String part : paramParts) {
                        part = part.trim();
                        String[] typeName = part.split("\\s+");
                        if (typeName.length >= 2) {
                            paramTypes.add(typeName[0]);
                            paramNames.add(typeName[1]);
                        }
                    }
                }

                return new MethodSignature(methodName, returnType, paramTypes, paramNames);
            }
        }
        
        // Fallback: Look for specific method names in the code
        if (userCode.contains("reverseString") && userCode.contains("String")) {
            System.out.println("Using reverseString fallback");
            return new MethodSignature("reverseString", "String", List.of("String"), List.of("s"));
        }
        if (userCode.contains("twoSum") && userCode.contains("int[]")) {
            System.out.println("Using twoSum fallback");
            return new MethodSignature("twoSum", "int[]", List.of("int[]", "int"), List.of("nums", "target"));
        }
        
        System.out.println("Using default solve fallback");
        return new MethodSignature("solve", "String", List.of("String"), List.of("s"));
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

    private ExecutionResult executeInDocker(Map<String, String> executionFiles, String language,
                                        int timeLimitMs, int memoryLimitMb) throws Exception {
    String imageName = getDockerImage(language);
    String containerName = "execution-" + System.currentTimeMillis();

    // ✅ Use user working directory to avoid Windows permission issues
    Path baseDir = Path.of(System.getProperty("user.dir"));
    Path codeDir = Files.createTempDirectory(baseDir, "execution");

    // ✅ Write all provided files to the temporary code directory
    for (Map.Entry<String, String> entry : executionFiles.entrySet()) {
        Path filePath = codeDir.resolve(entry.getKey());
        Files.write(filePath, entry.getValue().getBytes());
    }

    try {
        // ✅ Get the execution command for the given language
        String executionCommand = getExecutionCommand(language);

        // ✅ Convert path to Docker-friendly format
        String volumePath = codeDir.toAbsolutePath().toString();
        if (System.getProperty("os.name").toLowerCase().contains("win")) {
            // Convert Windows path (C:\Users\...) → Docker-friendly (/c/Users/...)
            volumePath = volumePath.replace("\\", "/");
            if (volumePath.matches("^[A-Za-z]:/.*")) {
                volumePath = "/" + Character.toLowerCase(volumePath.charAt(0)) + volumePath.substring(2);
            }
        }

        // ✅ Determine if shell execution is required
        boolean needsShell =
                language.equalsIgnoreCase("JAVA") ||
                language.equalsIgnoreCase("CPP") ||
                language.equalsIgnoreCase("C") ||
                executionCommand.contains("&&") ||
                executionCommand.contains(";") ||
                executionCommand.contains("cd");

        // ✅ Build Docker run command
        ProcessBuilder pb;
        if (needsShell) {
            pb = new ProcessBuilder(
                    "docker", "run", "--rm",
                    "--name", containerName,
                    "--memory", memoryLimitMb + "m",
                    "--memory-swap", memoryLimitMb + "m",
                    "--cpus", "0.5",
                    "--user", "root",
                    "-v", volumePath + ":/code:rw",
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
                    "--user", "root",
                    "-v", volumePath + ":/code:rw",
                    imageName,
                    executionCommand
            );
        }

        // ✅ Debug logs for transparency
        System.out.println("=== DEBUG: Docker Execution ===");
        System.out.println("Command: " + String.join(" ", pb.command()));
        System.out.println("Language: " + language);
        System.out.println("Execution Command: " + executionCommand);
        System.out.println("Using Shell: " + needsShell);
        System.out.println("Volume Path: " + volumePath);

        // ✅ Start the Docker process
        Process process = pb.start();

        // ✅ Enforce time limit with grace period
        boolean finished = process.waitFor(timeLimitMs + 5000, TimeUnit.MILLISECONDS);

        if (!finished) {
            // Kill stuck container
            new ProcessBuilder("docker", "kill", containerName).start().waitFor();
            return new ExecutionResult(null, true, 0, 0, "Time limit exceeded");
        }

        // ✅ Read process output & error
        String output = readStream(process.getInputStream());
        String error = readStream(process.getErrorStream());
        int exitCode = process.exitValue();

        System.out.println("=== DEBUG: Execution Output ===");
        System.out.println("Exit Code: " + exitCode);
        System.out.println("Output: " + output);
        System.out.println("Error: " + error);

        // ✅ Handle non-zero exit codes as runtime errors
        if (exitCode != 0) {
            return new ExecutionResult(null, false, 0, 0, "Runtime error: " + error);
        }

        // ✅ Simulated metrics (you can later measure real resource usage)
        int execTime = Math.min(timeLimitMs, 50 + (int)(Math.random() * 100));
        int memory = Math.min(memoryLimitMb, 10 + (int)(Math.random() * 20));

        return new ExecutionResult(output, false, execTime, memory, null);

    } finally {
        // ✅ Cleanup temporary directory after use
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
            case "JAVA" -> "cd /tmp && cp -r /code/* /tmp/ && javac *.java && java Main";
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
            System.out.println("=== DEBUG: Comparing Outputs ===");
            System.out.println("Actual: '" + actual + "'");
            System.out.println("Expected: '" + expected + "'");
            
            // Handle string outputs - if expected is quoted JSON string, unquote it for comparison
            String normalizedActual = normalizeOutput(actual);
            String normalizedExpected = normalizeOutput(expected);
            
            // If expected output is a JSON string with quotes, remove them for comparison
            if (normalizedExpected.startsWith("\"") && normalizedExpected.endsWith("\"")) {
                normalizedExpected = normalizedExpected.substring(1, normalizedExpected.length() - 1);
            }
            
            // If actual output is a JSON string with quotes, remove them for comparison
            if (normalizedActual.startsWith("\"") && normalizedActual.endsWith("\"")) {
                normalizedActual = normalizedActual.substring(1, normalizedActual.length() - 1);
            }
            
            System.out.println("Normalized Actual: '" + normalizedActual + "'");
            System.out.println("Normalized Expected: '" + normalizedExpected + "'");
            System.out.println("Match: " + normalizedActual.equals(normalizedExpected));
            
            return normalizedActual.equals(normalizedExpected);

        } catch (Exception e) {
            System.out.println("Output comparison error: " + e.getMessage());
            // Fallback to string comparison
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