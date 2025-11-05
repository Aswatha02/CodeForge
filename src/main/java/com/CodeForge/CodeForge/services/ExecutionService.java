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
import com.CodeForge.CodeForge.dto.SubmissionExecutionResponse;
import com.CodeForge.CodeForge.model.TestCase;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.stream.Collectors;

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
                case "JAVA" -> Map.of("Main.java", createJavaExecutionCode(userCode, inputNode));
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
        sb.append(userCode.trim()).append("\n\n");
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

        // Extract function name from user code
        String functionName = extractJavaScriptFunctionName(userCode);
        
        sb.append("const result = ").append(functionName).append("(");
        sb.append(String.join(", ", paramNames));
        sb.append(");\n");

        sb.append("console.log(JSON.stringify(result));");
        return sb.toString();
    }
    
    /**
     * Extract the function name from JavaScript code
     */
    private String extractJavaScriptFunctionName(String code) {
        // Look for patterns: function functionName( or const functionName = or var functionName =
        Pattern[] patterns = {
            Pattern.compile("function\\s+(\\w+)\\s*\\("),
            Pattern.compile("const\\s+(\\w+)\\s*=\\s*function"),
            Pattern.compile("const\\s+(\\w+)\\s*=\\s*\\("),
            Pattern.compile("var\\s+(\\w+)\\s*=\\s*function"),
            Pattern.compile("let\\s+(\\w+)\\s*=\\s*function")
        };
        
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(code);
            if (matcher.find()) {
                return matcher.group(1);
            }
        }
        // Default fallback
        return "solve";
    }

    private String createPythonExecutionCode(String userCode, JsonNode inputNode) {
        StringBuilder sb = new StringBuilder();
        
        // Import json at the top
        sb.append("import json\n\n");
        
        // Add user code (ensure no leading/trailing whitespace issues)
        sb.append(userCode.trim()).append("\n\n");
        
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

        // Check if code uses a class (like "class Solution:")
        boolean isClassBased = userCode.contains("class Solution");
        
        if (isClassBased) {
            // Extract method name from class
            String methodName = extractPythonFunctionName(userCode);
            
            // Create instance and call method
            sb.append("solution = Solution()\n");
            sb.append("result = solution.").append(methodName).append("(");
            sb.append(String.join(", ", paramNames));
            sb.append(")\n");
        } else {
            // Standalone function
            String functionName = extractPythonFunctionName(userCode);
            sb.append("result = ").append(functionName).append("(");
            sb.append(String.join(", ", paramNames));
            sb.append(")\n");
        }

        sb.append("print(json.dumps(result))");
        return sb.toString();
    }
    
    /**
     * Extract the function name from Python code
     */
    private String extractPythonFunctionName(String code) {
        // Look for pattern: def functionName(
        Pattern pattern = Pattern.compile("def\\s+(\\w+)\\s*\\(");
        Matcher matcher = pattern.matcher(code);
        if (matcher.find()) {
            return matcher.group(1);
        }
        // Default fallback
        return "solve";
    }
    
    /**
     * Extract the function name from C++ code
     */
    private String extractCppFunctionName(String code) {
        // Look for patterns: returnType functionName( or auto functionName(
        Pattern[] patterns = {
            Pattern.compile("(?:string|char\\*|int\\*|int|void|auto|vector<\\w+>|bool)\\s+(\\w+)\\s*\\("),
            Pattern.compile("std::string\\s+(\\w+)\\s*\\(")
        };
        
        for (Pattern pattern : patterns) {
            Matcher matcher = pattern.matcher(code);
            if (matcher.find()) {
                String funcName = matcher.group(1);
                // Skip main function
                if (!funcName.equals("main")) {
                    return funcName;
                }
            }
        }
        // Default fallback
        return "solve";
    }
    
    /**
     * Extract the function name and return type from C code
     */
    private String[] extractCFunctionInfo(String code) {
        // Look for patterns: returnType functionName(
        Pattern pattern = Pattern.compile("(char\\*|int\\*|int|void)\\s+(\\w+)\\s*\\(");
        Matcher matcher = pattern.matcher(code);
        
        while (matcher.find()) {
            String returnType = matcher.group(1);
            String funcName = matcher.group(2);
            // Skip main function
            if (!funcName.equals("main")) {
                return new String[]{funcName, returnType};
            }
        }
        // Default fallback
        return new String[]{"solve", "int*"};
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

    private String normalizeJavaClassName(String userCode) {
        // Replace any class declaration with "class Solution" to ensure consistent naming
        // This handles cases where the class might be named differently or have public modifier
        return userCode.replaceAll("(?i)public\\s+class\\s+\\w+", "class Solution")
                      .replaceAll("(?i)class\\s+\\w+", "class Solution");
    }

    private Map<String, String> createJavaExecutionFiles(String userCode, JsonNode inputNode) {
        Map<String, String> files = new HashMap<>();

        // Solution.java - user's code with class name normalized to Solution
        String modifiedUserCode = normalizeJavaClassName(userCode);
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
        
        // Default fallback
        return new MethodSignature("solve", "String", List.of("String"), List.of("s"));
    }

    private String createCppExecutionCode(String userCode, JsonNode inputNode) {
        StringBuilder sb = new StringBuilder();

        // Add necessary includes
        sb.append("#include <iostream>\n");
        sb.append("#include <vector>\n");
        sb.append("#include <string>\n");
        sb.append("#include <algorithm>\n");
        sb.append("#include <sstream>\n");
        sb.append("#include <cstring>\n");
        sb.append("using namespace std;\n\n");

        // Add user's code
        sb.append(userCode.trim()).append("\n\n");
        
        // Check if code uses a class (like "class Solution")
        boolean isClassBased = userCode.contains("class Solution");
        
        // Extract function name from user code
        String functionName = extractCppFunctionName(userCode);

        // Create main function
        sb.append("int main() {\n");
        
        // Create instance if class-based
        if (isClassBased) {
            sb.append("    Solution solution;\n");
        }

        // Create variables from input JSON
        List<String> paramNames = new ArrayList<>();
        inputNode.fieldNames().forEachRemaining(fieldName -> {
            JsonNode fieldValue = inputNode.get(fieldName);

            if (fieldValue.isArray()) {
                sb.append("    vector<int> ").append(fieldName).append(" = {");
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
                sb.append("    string ").append(fieldName).append(" = \"").append(fieldValue.asText()).append("\";\n");
                paramNames.add(fieldName);
            }
        });

        sb.append("\n");

        // Call user's function
        sb.append("    // Call user's solution\n");
        if (isClassBased) {
            sb.append("    auto result = solution.").append(functionName).append("(");
        } else {
            sb.append("    auto result = ").append(functionName).append("(");
        }
        sb.append(String.join(", ", paramNames));
        sb.append(");\n\n");

        // Output the result (handle string return type)
        sb.append("    // Output result as JSON\n");
        sb.append("    cout << \"\\\"\" << result << \"\\\"\" << endl;\n");
        sb.append("    return 0;\n");
        sb.append("}");

        return sb.toString();
    }
    
    private String createCExecutionCode(String userCode, JsonNode inputNode) {
        StringBuilder sb = new StringBuilder();

        // Add necessary includes
        sb.append("#include <stdio.h>\n");
        sb.append("#include <stdlib.h>\n");
        sb.append("#include <string.h>\n\n");

        // Add user's code
        sb.append(userCode.trim()).append("\n\n");
        
        // Extract function name and return type
        String[] funcInfo = extractCFunctionInfo(userCode);
        String functionName = funcInfo[0];
        String returnType = funcInfo[1];

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
        sb.append("    ").append(returnType).append(" result = ").append(functionName).append("(");
        List<String> callParams = new ArrayList<>();
        for (String param : paramNames) {
            callParams.add(param);
            if (param.equals(arrayNameHolder[0])) {
                callParams.add(param + "_size");
            }
        }
        sb.append(String.join(", ", callParams));
        sb.append(");\n\n");

        // Output the result based on return type
        sb.append("    // Output result as JSON\n");
        if (returnType.equals("char*")) {
            sb.append("    printf(\"\\\"%s\\\"\\n\", result);\n");
        } else if (arraySizeHolder[0] > 0) {
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

    // ✅ Use system temp directory for better Docker mount compatibility
    Path baseDir = Path.of(System.getProperty("java.io.tmpdir"));
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
            // Split the execution command into separate arguments
            String[] commandParts = executionCommand.split("\\s+");
            List<String> commandList = new ArrayList<>();
            commandList.addAll(List.of("docker", "run", "--rm",
                    "--name", containerName,
                    "--memory", memoryLimitMb + "m",
                    "--memory-swap", memoryLimitMb + "m",
                    "--cpus", "0.5",
                    "--user", "root",
                    "-v", volumePath + ":/code:rw",
                    imageName));
            commandList.addAll(List.of(commandParts));
            pb = new ProcessBuilder(commandList);
        }

        // ✅ Debug logs for transparency
        System.out.println("=== DEBUG: Docker Execution ===");
        System.out.println("Command: " + String.join(" ", pb.command()));
        System.out.println("Language: " + language);
        System.out.println("Execution Command: " + executionCommand);
        System.out.println("Using Shell: " + needsShell);
        System.out.println("Volume Path: " + volumePath);

        // ✅ Start the Docker process directly (no need for OS shell wrapper)
        // The Docker container has its own shell (/bin/sh -c) for commands that need it
        Process process = pb.redirectErrorStream(true).start();

        // ✅ Enforce time limit with grace period
        boolean finished = process.waitFor(timeLimitMs + 5000, TimeUnit.MILLISECONDS);

        if (!finished) {
            // Kill stuck container
            new ProcessBuilder("docker", "kill", containerName).start().waitFor();
            return new ExecutionResult(null, true, 0, 0, "Time limit exceeded");
        }

        // ✅ Read process output & error (redirectErrorStream merges them)
        String output = readStream(process.getInputStream());
        String error = readStream(process.getErrorStream());
        int exitCode = process.exitValue();

        System.out.println("=== DEBUG: Execution Output ===");
        System.out.println("Exit Code: " + exitCode);
        System.out.println("Output: " + output);
        System.out.println("Error: " + error);

        // ✅ Handle non-zero exit codes as runtime errors
        if (exitCode != 0) {
            // With redirectErrorStream(true), all output including errors is in output stream
            String errorMessage = output.isEmpty() ? error : output;
            return new ExecutionResult(null, false, 0, 0, "Runtime error: " + errorMessage);
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


    private String getExecutionCommand(String language) {
        return switch (language.toUpperCase()) {
            case "JAVASCRIPT" -> "node /code/main.js";
            case "PYTHON" -> "python /code/main.py";
            case "JAVA" -> "cd /code && (test -f Solution.java && javac Solution.java Main.java || javac Main.java) && java Main";
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

    private Map<String, String> createBatchExecutionCode(String combinedCode, String language, List<TestCase> testCases) {
        try {
            return switch (language.toUpperCase()) {
                case "JAVASCRIPT" -> Map.of("main.js", createBatchJavaScriptExecutionCode(combinedCode, testCases));
                case "PYTHON" -> Map.of("main.py", createBatchPythonExecutionCode(combinedCode, testCases));
                case "JAVA" -> createBatchJavaExecutionFiles(combinedCode, testCases);
                case "CPP" -> Map.of("main.cpp", createBatchCppExecutionCode(combinedCode, testCases));
                case "C" -> Map.of("main.c", createBatchCExecutionCode(combinedCode, testCases));
                default -> throw new IllegalArgumentException("Unsupported language: " + language);
            };
        } catch (Exception e) {
            throw new RuntimeException("Failed to create batch execution code: " + e.getMessage(), e);
        }
    }

    private String createBatchJavaScriptExecutionCode(String userCode, List<TestCase> testCases) {
        StringBuilder sb = new StringBuilder();
        sb.append(userCode.trim()).append("\n\n");
        
        // Extract function name from user code
        String functionName = extractJavaScriptFunctionName(userCode);
        
        sb.append("// Batch test execution\n");
        sb.append("const results = [];\n\n");

        for (int i = 0; i < testCases.size(); i++) {
            TestCase tc = testCases.get(i);
            try {
                JsonNode inputNode = objectMapper.readTree(tc.getInputData());

                sb.append("// Test case ").append(i + 1).append("\n");
                sb.append("try {\n");

                // Extract individual parameters from JSON
                List<String> paramNames = new ArrayList<>();
                inputNode.fieldNames().forEachRemaining(fieldName -> {
                    JsonNode fieldValue = inputNode.get(fieldName);
                    sb.append("    const ").append(fieldName).append(" = ");

                    if (fieldValue.isTextual()) {
                        sb.append("\"").append(fieldValue.asText()).append("\"");
                    } else {
                        sb.append(fieldValue.toString());
                    }
                    sb.append(";\n");
                    paramNames.add(fieldName);
                });

                // Call function and store result
                sb.append("    const result").append(i).append(" = ").append(functionName).append("(");
                sb.append(String.join(", ", paramNames));
                sb.append(");\n");
                sb.append("    results.push(result").append(i).append(");\n");
                sb.append("} catch (error) {\n");
                sb.append("    results.push(\"ERROR: \" + error.message);\n");
                sb.append("}\n\n");

            } catch (Exception e) {
                sb.append("    results.push(\"INPUT_PARSE_ERROR\");\n\n");
            }
        }

        sb.append("console.log(JSON.stringify(results));");
        return sb.toString();
    }

    private String createBatchPythonExecutionCode(String userCode, List<TestCase> testCases) {
        StringBuilder sb = new StringBuilder();
        
        // Import json at the top
        sb.append("import json\n\n");
        
        // Add user code (trimmed to avoid whitespace issues)
        sb.append(userCode.trim()).append("\n\n");
        
        sb.append("# Batch test execution\n");
        sb.append("results = []\n\n");
        
        // Check if code uses a class (like "class Solution:")
        boolean isClassBased = userCode.contains("class Solution");
        String functionName = extractPythonFunctionName(userCode);

        for (int i = 0; i < testCases.size(); i++) {
            TestCase tc = testCases.get(i);
            try {
                JsonNode inputNode = objectMapper.readTree(tc.getInputData());

                sb.append("# Test case ").append(i + 1).append("\n");
                sb.append("try:\n");

                // Extract individual parameters from JSON
                List<String> paramNames = new ArrayList<>();
                inputNode.fieldNames().forEachRemaining(fieldName -> {
                    JsonNode fieldValue = inputNode.get(fieldName);
                    sb.append("    ").append(fieldName).append(" = ");

                    if (fieldValue.isTextual()) {
                        sb.append("\"").append(fieldValue.asText()).append("\"");
                    } else {
                        sb.append(fieldValue.toString());
                    }
                    sb.append("\n");
                    paramNames.add(fieldName);
                });

                // Call function and store result
                if (isClassBased) {
                    // For first test case, create instance
                    if (i == 0) {
                        sb.append("    solution = Solution()\n");
                    }
                    sb.append("    result").append(i).append(" = solution.").append(functionName).append("(");
                } else {
                    sb.append("    result").append(i).append(" = ").append(functionName).append("(");
                }
                sb.append(String.join(", ", paramNames));
                sb.append(")\n");
                sb.append("    results.append(result").append(i).append(")\n");
                sb.append("except Exception as error:\n");
                sb.append("    results.append(\"ERROR: \" + str(error))\n\n");

            } catch (Exception e) {
                sb.append("    results.append(\"INPUT_PARSE_ERROR\")\n\n");
            }
        }

        sb.append("print(json.dumps(results))");
        return sb.toString();
    }

    private Map<String, String> createBatchJavaExecutionFiles(String userCode, List<TestCase> testCases) {
        Map<String, String> files = new HashMap<>();

        if (userCode.contains("public static void main")) {
            // Full executable code - use as Main.java
            files.put("Main.java", userCode);
            return files;
        } else {
            // Solution.java - user's code with class name normalized to Solution
            String modifiedUserCode = normalizeJavaClassName(userCode);
            files.put("Solution.java", modifiedUserCode);

        // Parse method signature from user code
        MethodSignature methodSig = parseJavaMethodSignature(userCode);

        // Main.java - the batch execution wrapper
        StringBuilder mainSb = new StringBuilder();
        mainSb.append("import java.util.*;\n\n");
        mainSb.append("public class Main {\n");
        mainSb.append("    public static void main(String[] args) throws Exception {\n");
        mainSb.append("        Solution solution = new Solution();\n");
        mainSb.append("        List<String> results = new ArrayList<>();\n\n");

        for (int i = 0; i < testCases.size(); i++) {
            TestCase tc = testCases.get(i);
            try {
                JsonNode inputNode = objectMapper.readTree(tc.getInputData());

                mainSb.append("        // Test case ").append(i + 1).append("\n");
                mainSb.append("        try {\n");

                // Create variables from input JSON
                List<String> paramNames = new ArrayList<>();
                inputNode.fieldNames().forEachRemaining(fieldName -> {
                    JsonNode fieldValue = inputNode.get(fieldName);

                    if (fieldValue.isArray()) {
                        mainSb.append("            int[] ").append(fieldName).append(" = new int[]{");
                        for (int j = 0; j < fieldValue.size(); j++) {
                            if (j > 0) mainSb.append(", ");
                            mainSb.append(fieldValue.get(j).asInt());
                        }
                        mainSb.append("};\n");
                        paramNames.add(fieldName);
                    } else if (fieldValue.isNumber()) {
                        mainSb.append("            int ").append(fieldName).append(" = ").append(fieldValue.asInt()).append(";\n");
                        paramNames.add(fieldName);
                    } else if (fieldValue.isTextual()) {
                        mainSb.append("            String ").append(fieldName).append(" = \"").append(fieldValue.asText().replace("\"", "\\\"")).append("\";\n");
                        paramNames.add(fieldName);
                    } else if (fieldValue.isBoolean()) {
                        mainSb.append("            boolean ").append(fieldName).append(" = ").append(fieldValue.asBoolean()).append(";\n");
                        paramNames.add(fieldName);
                    }
                });

                // Call user's function and store result
                mainSb.append("            ").append(methodSig.returnType).append(" result").append(i).append(" = solution.")
                      .append(methodSig.methodName).append("(")
                      .append(String.join(", ", paramNames)).append(");\n");
                
                // Convert result to JSON string based on type
                if (methodSig.returnType.equals("String")) {
                    mainSb.append("            results.add(\"\\\"\" + result").append(i).append(" + \"\\\"\");\n");
                } else if (methodSig.returnType.equals("int[]")) {
                    mainSb.append("            StringBuilder sb").append(i).append(" = new StringBuilder(\"[\");\n");
                    mainSb.append("            for (int j = 0; j < result").append(i).append(".length; j++) {\n");
                    mainSb.append("                if (j > 0) sb").append(i).append(".append(\",\");\n");
                    mainSb.append("                sb").append(i).append(".append(result").append(i).append("[j]);\n");
                    mainSb.append("            }\n");
                    mainSb.append("            sb").append(i).append(".append(\"]\");\n");
                    mainSb.append("            results.add(sb").append(i).append(".toString());\n");
                } else {
                    mainSb.append("            results.add(String.valueOf(result").append(i).append("));\n");
                }
                
                mainSb.append("        } catch (Exception e) {\n");
                mainSb.append("            results.add(\"\\\"ERROR: \" + e.getMessage() + \"\\\"\");\n");
                mainSb.append("        }\n\n");

            } catch (Exception e) {
                mainSb.append("        results.add(\"\\\"INPUT_PARSE_ERROR\\\"\");\n\n");
            }
        }

        // Output results as JSON array manually
        mainSb.append("        // Output results\n");
        mainSb.append("        System.out.print(\"[\");\n");
        mainSb.append("        for (int i = 0; i < results.size(); i++) {\n");
        mainSb.append("            if (i > 0) System.out.print(\",\");\n");
        mainSb.append("            System.out.print(results.get(i));\n");
        mainSb.append("        }\n");
        mainSb.append("        System.out.println(\"]\");\n");
        mainSb.append("    }\n");
        mainSb.append("}");

        files.put("Main.java", mainSb.toString());
    }

    return files;
}

    private String createBatchCppExecutionCode(String userCode, List<TestCase> testCases) {
        StringBuilder sb = new StringBuilder();

        // Add necessary includes
        sb.append("#include <iostream>\n");
        sb.append("#include <vector>\n");
        sb.append("#include <string>\n");
        sb.append("#include <sstream>\n");
        sb.append("#include <algorithm>\n");
        sb.append("#include <cstring>\n");
        sb.append("using namespace std;\n\n");

        // Add user's code
        sb.append(userCode.trim()).append("\n\n");
        
        // Check if code uses a class (like "class Solution")
        boolean isClassBased = userCode.contains("class Solution");
        
        // Extract function name from user code
        String functionName = extractCppFunctionName(userCode);

        // Create main function
        sb.append("int main() {\n");
        
        // Create instance if class-based
        if (isClassBased) {
            sb.append("    Solution solution;\n");
        }
        
        sb.append("    vector<string> results;\n\n");

        for (int i = 0; i < testCases.size(); i++) {
            TestCase tc = testCases.get(i);
            try {
                JsonNode inputNode = objectMapper.readTree(tc.getInputData());

                sb.append("    // Test case ").append(i + 1).append("\n");
                sb.append("    try {\n");

                // Create variables from input JSON
                List<String> paramNames = new ArrayList<>();
                inputNode.fieldNames().forEachRemaining(fieldName -> {
                    JsonNode fieldValue = inputNode.get(fieldName);

                    if (fieldValue.isArray()) {
                        sb.append("        vector<int> ").append(fieldName).append(" = {");
                        for (int j = 0; j < fieldValue.size(); j++) {
                            if (j > 0) sb.append(", ");
                            sb.append(fieldValue.get(j).asInt());
                        }
                        sb.append("};\n");
                        paramNames.add(fieldName);
                    } else if (fieldValue.isNumber()) {
                        sb.append("        int ").append(fieldName).append(" = ").append(fieldValue.asInt()).append(";\n");
                        paramNames.add(fieldName);
                    } else if (fieldValue.isTextual()) {
                        sb.append("        string ").append(fieldName).append(" = \"").append(fieldValue.asText()).append("\";\n");
                        paramNames.add(fieldName);
                    }
                });

                // Call user's function and store result
                if (isClassBased) {
                    sb.append("        auto result").append(i).append(" = solution.").append(functionName).append("(");
                } else {
                    sb.append("        auto result").append(i).append(" = ").append(functionName).append("(");
                }
                sb.append(String.join(", ", paramNames));
                sb.append(");\n");

                // Convert result to string and add to results
                // Handle string return type
                sb.append("        stringstream ss").append(i).append(";\n");
                sb.append("        ss").append(i).append(" << \"\\\"\" << result").append(i).append(" << \"\\\"\";\n");
                sb.append("        results.push_back(ss").append(i).append(".str());\n");

                sb.append("    } catch (...) {\n");
                sb.append("        results.push_back(\"\\\"ERROR\\\"\");\n");
                sb.append("    }\n\n");

            } catch (Exception e) {
                sb.append("    results.push_back(\"\\\"INPUT_PARSE_ERROR\\\"\");\n\n");
            }
        }

        // Output results as JSON array
        sb.append("    // Output results\n");
        sb.append("    cout << \"[\";\n");
        sb.append("    for (size_t i = 0; i < results.size(); ++i) {\n");
        sb.append("        if (i > 0) cout << \",\";\n");
        sb.append("        cout << results[i];\n");
        sb.append("    }\n");
        sb.append("    cout << \"]\" << endl;\n");
        sb.append("    return 0;\n");
        sb.append("}");

        return sb.toString();
    }

    private String createBatchCExecutionCode(String userCode, List<TestCase> testCases) {
        StringBuilder sb = new StringBuilder();

        // Add necessary includes
        sb.append("#include <stdio.h>\n");
        sb.append("#include <stdlib.h>\n");
        sb.append("#include <string.h>\n\n");

        // Add user's code
        sb.append(userCode.trim()).append("\n\n");
        
        // Extract function name and return type
        String[] funcInfo = extractCFunctionInfo(userCode);
        String functionName = funcInfo[0];
        String returnType = funcInfo[1];
        
        // Debug: Log test case data
        System.out.println("=== DEBUG: Creating Batch C Code ===");
        System.out.println("Function: " + functionName + ", Return Type: " + returnType);
        for (int i = 0; i < testCases.size(); i++) {
            System.out.println("Test Case " + i + ": " + testCases.get(i).getInputData());
        }

        // Create main function
        sb.append("int main() {\n");
        
        // Declare all buffers at the beginning so they don't go out of scope
        for (int i = 0; i < testCases.size(); i++) {
            sb.append("    char buffer").append(i).append("[256];\n");
        }
        sb.append("    const char* results[").append(testCases.size()).append("];\n");
        sb.append("    int result_count = 0;\n\n");

        for (int i = 0; i < testCases.size(); i++) {
            TestCase tc = testCases.get(i);
            try {
                JsonNode inputNode = objectMapper.readTree(tc.getInputData());

                sb.append("    // Test case ").append(i + 1).append("\n");
                sb.append("    {\n");

                // Create variables from input JSON
                List<String> paramNames = new ArrayList<>();
                final int[] arraySizeHolder = {0};
                final String[] arrayNameHolder = {null};
                final int testIndex = i;  // Make i effectively final for lambda

                inputNode.fieldNames().forEachRemaining(fieldName -> {
                    JsonNode fieldValue = inputNode.get(fieldName);

                    if (fieldValue.isArray()) {
                        sb.append("        int ").append(fieldName).append(testIndex).append("[] = {");
                        for (int j = 0; j < fieldValue.size(); j++) {
                            if (j > 0) sb.append(", ");
                            sb.append(fieldValue.get(j).asInt());
                        }
                        sb.append("};\n");
                        arraySizeHolder[0] = fieldValue.size();
                        arrayNameHolder[0] = fieldName;
                        paramNames.add(fieldName + testIndex);
                    } else if (fieldValue.isNumber()) {
                        sb.append("        int ").append(fieldName).append(testIndex).append(" = ").append(fieldValue.asInt()).append(";\n");
                        paramNames.add(fieldName + testIndex);
                    } else if (fieldValue.isTextual()) {
                        sb.append("        char* ").append(fieldName).append(testIndex).append(" = \"").append(fieldValue.asText()).append("\";\n");
                        paramNames.add(fieldName + testIndex);
                    }
                });

                if (arrayNameHolder[0] != null) {
                    sb.append("        int ").append(arrayNameHolder[0]).append(i).append("_size = ").append(arraySizeHolder[0]).append(";\n");
                }

                // Call user's function and store result
                sb.append("        ").append(returnType).append(" result").append(i).append(" = ").append(functionName).append("(");
                List<String> callParams = new ArrayList<>();
                for (String param : paramNames) {
                    callParams.add(param);
                    if (param.equals(arrayNameHolder[0] + i)) {
                        callParams.add(arrayNameHolder[0] + i + "_size");
                    }
                }
                sb.append(String.join(", ", callParams));
                sb.append(");\n");

                // Convert result to string and add to results
                // Buffer already declared at the top of main()
                if (returnType.equals("char*")) {
                    sb.append("        sprintf(buffer").append(i).append(", \"\\\"%s\\\"\", result").append(i).append(");\n");
                } else if (arraySizeHolder[0] > 0) {
                    sb.append("        sprintf(buffer").append(i).append(", \"[\");\n");
                    sb.append("        for (int j = 0; j < ").append(arraySizeHolder[0]).append("; ++j) {\n");
                    sb.append("            char temp[16];\n");
                    sb.append("            sprintf(temp, \"%d\", result").append(i).append("[j]);\n");
                    sb.append("            if (j > 0) strcat(buffer").append(i).append(", \",\");\n");
                    sb.append("            strcat(buffer").append(i).append(", temp);\n");
                    sb.append("        }\n");
                    sb.append("        strcat(buffer").append(i).append(", \"]\");\n");
                } else {
                    sb.append("        sprintf(buffer").append(i).append(", \"%d\", *result").append(i).append(");\n");
                }
                sb.append("        results[result_count++] = buffer").append(i).append(";\n");
                sb.append("    }\n\n");

            } catch (Exception e) {
                sb.append("    results[result_count++] = \"INPUT_PARSE_ERROR\";\n\n");
            }
        }

        // Output results as JSON array
        sb.append("    // Output results\n");
        sb.append("    printf(\"[\");\n");
        sb.append("    for (int i = 0; i < result_count; ++i) {\n");
        sb.append("        if (i > 0) printf(\",\");\n");
        sb.append("        printf(\"%s\", results[i]);\n");
        sb.append("    }\n");
        sb.append("    printf(\"]\\n\");\n");
        sb.append("    return 0;\n");
        sb.append("}");

        String generatedCode = sb.toString();
        System.out.println("=== DEBUG: Generated C Code ===");
        System.out.println(generatedCode);
        System.out.println("=== END Generated C Code ===");
        
        return generatedCode;
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
     * Execute submission code against multiple test cases in batch (single container execution)
     */
    public SubmissionExecutionResponse executeSubmissionCode(String combinedCode, String language,
                                                          List<TestCase> testCases, int timeLimitMs, int memoryLimitMb, String functionName) {
        SubmissionExecutionResponse response = new SubmissionExecutionResponse();
        List<SubmissionExecutionResponse.TestCaseResult> results = new ArrayList<>();

        try {
            // Create batch execution code
            Map<String, String> executionFiles = createBatchExecutionCode(combinedCode, language, testCases);

            // Execute all test cases in single Docker container
            ExecutionResult batchResult = executeInDocker(executionFiles, language, timeLimitMs * testCases.size(), memoryLimitMb); // adjust time limit

            if (batchResult.timedOut) {
                // If timed out, mark all as timed out
                for (TestCase tc : testCases) {
                    SubmissionExecutionResponse.TestCaseResult result = new SubmissionExecutionResponse.TestCaseResult();
                    result.setTestCaseId(tc.getId());
                    result.setTimedOut(true);
                    result.setError("Time limit exceeded");
                    results.add(result);
                }
            } else if (batchResult.error != null) {
                // If compilation/runtime error, mark all as error
                for (TestCase tc : testCases) {
                    SubmissionExecutionResponse.TestCaseResult result = new SubmissionExecutionResponse.TestCaseResult();
                    result.setTestCaseId(tc.getId());
                    result.setError(batchResult.error);
                    results.add(result);
                }
            } else {
                // Parse batch output as JSON array
                try {
                    JsonNode resultsArray = objectMapper.readTree(batchResult.output);
                    if (resultsArray.isArray()) {
                        for (int i = 0; i < testCases.size() && i < resultsArray.size(); i++) {
                            TestCase tc = testCases.get(i);
                            JsonNode actualResult = resultsArray.get(i);
                            String actualOutput = actualResult.toString();

                            // Compare with expected
                            String normalizedActual = normalizeOutput(actualOutput);
                            String normalizedExpected = normalizeOutput(tc.getExpectedOutput());

                            SubmissionExecutionResponse.TestCaseResult result = new SubmissionExecutionResponse.TestCaseResult();
                            result.setTestCaseId(tc.getId());

                            if (compareOutputs(normalizedActual, normalizedExpected)) {
                                result.setPassed(true);
                                result.setActualOutput(normalizedActual);
                            } else {
                                result.setPassed(false);
                                result.setActualOutput(normalizedActual);
                            }

                            results.add(result);
                        }
                    } else {
                        // If not array, treat as error
                        for (TestCase tc : testCases) {
                            SubmissionExecutionResponse.TestCaseResult result = new SubmissionExecutionResponse.TestCaseResult();
                            result.setTestCaseId(tc.getId());
                            result.setError("Invalid output format");
                            results.add(result);
                        }
                    }
                } catch (Exception e) {
                    // If parsing fails, treat as runtime error
                    for (TestCase tc : testCases) {
                        SubmissionExecutionResponse.TestCaseResult result = new SubmissionExecutionResponse.TestCaseResult();
                        result.setTestCaseId(tc.getId());
                        result.setError("Output parsing error: " + e.getMessage());
                        results.add(result);
                    }
                }
            }

            response.setTestCaseResults(results);
            response.setTotalExecutionTime(batchResult.executionTime);
            response.setMaxMemoryUsed(batchResult.memoryUsed);

        } catch (Exception e) {
            response.setError("Internal execution error: " + e.getMessage());
        }

        return response;
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