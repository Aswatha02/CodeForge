package com.CodeForge.CodeForge.services;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.CodeForge.CodeForge.dto.SubmissionExecutionResponse;
import com.CodeForge.CodeForge.model.CodeTemplate;
import com.CodeForge.CodeForge.model.Contest;
import com.CodeForge.CodeForge.model.ContestParticipant;
import com.CodeForge.CodeForge.model.ContestProblem;
import com.CodeForge.CodeForge.model.Problem;
import com.CodeForge.CodeForge.model.Submission;
import com.CodeForge.CodeForge.model.TestCase;
import com.CodeForge.CodeForge.model.User;
import com.CodeForge.CodeForge.repository.CodeTemplateRepository;
import com.CodeForge.CodeForge.repository.ContestParticipantRepository;
import com.CodeForge.CodeForge.repository.ContestProblemRepository;
import com.CodeForge.CodeForge.repository.ContestRepository;
import com.CodeForge.CodeForge.repository.ProblemRepository;
import com.CodeForge.CodeForge.repository.SubmissionRepository;
import com.CodeForge.CodeForge.repository.TestCaseRepository;
import com.CodeForge.CodeForge.repository.UserRepository;

@Service
@Transactional
public class SubmissionService {
    
    private final SubmissionRepository submissionRepository;
    private final ProblemRepository problemRepository;
    private final ContestRepository contestRepository;
    private final UserRepository userRepository;
    private final ContestProblemRepository contestProblemRepository;
    private final ContestParticipantRepository contestParticipantRepository;
    private final TestCaseRepository testCaseRepository;
    private final CodeTemplateRepository codeTemplateRepository;
    private final LeaderboardService leaderboardService;
    private final ObjectMapper objectMapper;
    private final FunctionSignatureService functionSignatureService;
    private final ExecutionService executionService;

    public SubmissionService(SubmissionRepository submissionRepository,
                           ProblemRepository problemRepository,
                           ContestRepository contestRepository,
                           UserRepository userRepository,
                           ContestProblemRepository contestProblemRepository,
                           ContestParticipantRepository contestParticipantRepository,
                           TestCaseRepository testCaseRepository,
                           CodeTemplateRepository codeTemplateRepository,
                           LeaderboardService leaderboardService,
                           FunctionSignatureService functionSignatureService,
                           ExecutionService executionService) {
        this.submissionRepository = submissionRepository;
        this.problemRepository = problemRepository;
        this.contestRepository = contestRepository;
        this.userRepository = userRepository;
        this.contestProblemRepository = contestProblemRepository;
        this.contestParticipantRepository = contestParticipantRepository;
        this.testCaseRepository = testCaseRepository;
        this.codeTemplateRepository = codeTemplateRepository;
        this.leaderboardService = leaderboardService;
        this.objectMapper = new ObjectMapper();
        this.functionSignatureService = functionSignatureService;
        this.executionService = executionService;
    }

    @Transactional
    public Submission submitCode(Long problemId, Long contestId, User user, String code, Submission.Language language) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("Problem not found"));

        // Validate code
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Code cannot be empty");
        }
        if (code.length() > 10000) {
            throw new IllegalArgumentException("Code too long");
        }

        ContestParticipant participant = null;
        ContestProblem contestProblem = null;
        if (contestId != null) {
            Contest contest = contestRepository.findById(contestId)
                    .orElseThrow(() -> new IllegalArgumentException("Contest not found"));
            
            // Check if contest is running
            if (contest.getStatus() != Contest.Status.RUNNING ||
                contest.getStartTime().isAfter(LocalDateTime.now()) ||
                contest.getEndTime().isBefore(LocalDateTime.now())) {
                throw new IllegalStateException("Contest is not active");
            }

            contestProblem = contestProblemRepository.findByContestAndProblem(contest, problem)
                    .orElseThrow(() -> new IllegalArgumentException("Problem not in contest"));

            participant = contestParticipantRepository.findByContestAndUser(contest, user)
                    .orElseThrow(() -> new IllegalStateException("User not registered for contest"));
        }

        // Create submission - using constructor that sets code and language
        Submission submission = new Submission(user, problem, code, language);
        
        submission = submissionRepository.save(submission);

        try {
            // Get all test cases for the problem (ordered by ID to ensure consistent execution order)
            List<TestCase> testCases = testCaseRepository.findByProblemOrderByIdAsc(problem);
            
            // Debug: Log test case data
            System.out.println("=== DEBUG: Test Cases for Problem " + problem.getId() + " ===");
            for (int i = 0; i < testCases.size(); i++) {
                TestCase tc = testCases.get(i);
                System.out.println("Test Case " + (i+1) + " (ID: " + tc.getId() + "):");
                System.out.println("  Input: " + tc.getInputData());
                System.out.println("  Expected: " + tc.getExpectedOutput());
            }
            
            if (testCases.isEmpty()) {
                submission.setStatus(Submission.Status.ACCEPTED);
                return submissionRepository.save(submission);
            }

            // Execute against all test cases
            submission = executeAgainstTestCases(submission, problem, testCases);
            
            // Update contest participant score if applicable
            if (contestId != null && submission.getStatus() == Submission.Status.ACCEPTED) {
                if (participant != null && contestProblem != null) {
                    // Use existing score calculation logic
                    int currentScore = Optional.ofNullable(participant.getScore()).orElse(0);
                    int problemPoints = Optional.ofNullable(contestProblem.getPoints()).orElse(10);
                    participant.setScore(currentScore + problemPoints);
                    contestParticipantRepository.save(participant);
                    leaderboardService.updateLeaderboard(contestId);
                }
            }

            return submissionRepository.save(submission);
        } catch (Exception e) {
            submission.setStatus(Submission.Status.RUNTIME_ERROR);
            submission.setErrorMessage("System error: " + e.getMessage());
            return submissionRepository.save(submission);
        }
    }

    private Submission executeAgainstTestCases(Submission submission, Problem problem, List<TestCase> testCases) {
        try {
            // Get the code template for this problem and language
            CodeTemplate.Language templateLanguage = switch (submission.getLanguage()) {
                case JAVA -> CodeTemplate.Language.JAVA;
                case JAVASCRIPT -> CodeTemplate.Language.JAVASCRIPT;
                case PYTHON -> CodeTemplate.Language.PYTHON;
                case CPP -> CodeTemplate.Language.CPP;
                case C -> CodeTemplate.Language.C;
            };
            CodeTemplate codeTemplate = codeTemplateRepository.findByProblemAndLanguage(problem, templateLanguage)
                .orElseThrow(() -> new IllegalArgumentException("Code template not found for problem and language"));

            // For now, always use the user's submitted code directly to avoid template indentation issues
            // TODO: Fix template system to properly handle indentation
            String executionCode = submission.getCode();

            // Detect function name from combined code
            String functionName = functionSignatureService.detectFunctionName(executionCode, submission.getLanguage());
            if (!functionSignatureService.isValidFunctionName(functionName)) {
                functionName = "solve"; // fallback
            }

            // Debug logging
            System.out.println("=== DEBUG: Submission Execution ===");
            System.out.println("Problem ID: " + problem.getId());
            System.out.println("Time limit: " + problem.getTimeLimitMs() + "ms");
            System.out.println("Memory limit: " + problem.getMemoryLimitMb() + "MB");
            System.out.println("Language: " + submission.getLanguage());
            System.out.println("Test cases count: " + testCases.size());
            System.out.println("Function name: " + functionName);
            System.out.println("Execution code length: " + executionCode.length());

            // Execute all test cases in batch using ExecutionService
            SubmissionExecutionResponse response = executionService.executeSubmissionCode(
                executionCode,
                submission.getLanguage().name(),
                testCases,
                problem.getTimeLimitMs(),
                problem.getMemoryLimitMb(),
                functionName
            );

            // Process batch execution results
            submission.setTotalTestCases(testCases.size());
            int passedCount = 0;
            boolean hasTimeLimit = false;
            boolean hasError = false;
            String errorMsg = null;

            for (SubmissionExecutionResponse.TestCaseResult result : response.getTestCaseResults()) {
                if (result.isTimedOut()) {
                    hasTimeLimit = true;
                    errorMsg = "Time limit exceeded";
                    break;
                }
                if (result.getError() != null) {
                    hasError = true;
                    errorMsg = result.getError();
                    break;
                }
                if (result.isPassed()) {
                    passedCount++;
                }
            }

            if (hasTimeLimit) {
                submission.setStatus(Submission.Status.TIME_LIMIT_EXCEEDED);
                submission.setErrorMessage(errorMsg);
            } else if (hasError) {
                if (errorMsg != null && errorMsg.contains("Compilation")) {
                    submission.setStatus(Submission.Status.COMPILATION_ERROR);
                } else {
                    submission.setStatus(Submission.Status.RUNTIME_ERROR);
                }
                submission.setErrorMessage(errorMsg);
            } else if (passedCount == testCases.size()) {
                submission.setStatus(Submission.Status.ACCEPTED);
                submission.setPassedTestCases(passedCount);
                submission.setExecutionTime(response.getTotalExecutionTime());
                submission.setMemoryUsed(response.getMaxMemoryUsed());
            } else {
                submission.setStatus(Submission.Status.WRONG_ANSWER);
                submission.setPassedTestCases(passedCount);
                // Set details from first failed test case
                for (SubmissionExecutionResponse.TestCaseResult result : response.getTestCaseResults()) {
                    if (!result.isPassed() && !result.isTimedOut() && result.getError() == null) {
                        submission.setActualOutput(result.getActualOutput());
                        // Find expected output from test cases
                        TestCase failedTestCase = testCases.stream()
                            .filter(tc -> tc.getId().equals(result.getTestCaseId()))
                            .findFirst()
                            .orElse(null);
                        if (failedTestCase != null) {
                            submission.setExpectedOutput(failedTestCase.getExpectedOutput());
                        }
                        break;
                    }
                }
            }

            return submission;

        } catch (Exception e) {
            submission.setStatus(Submission.Status.RUNTIME_ERROR);
            submission.setErrorMessage("System error: " + e.getMessage());
            return submission;
        }
    }

    private String combineUserCodeWithTemplate(String userCode, CodeTemplate codeTemplate) {
        // Replace the placeholder in hidden code with user code
        String hiddenCode = codeTemplate.getHiddenCode();
        return hiddenCode.replace("{{USER_CODE}}", userCode);
    }

    private ExecutionResult executeSingleTestCase(String code, Submission.Language language, 
                                               String inputJson, int timeLimitMs, int memoryLimitMb) throws Exception {
        
        // Create execution code based on language and problem function signature
        String executionCode = createExecutionCode(code, language, inputJson);
        
        return executeInDocker(executionCode, language, timeLimitMs, memoryLimitMb);
    }

    private String createExecutionCode(String userCode, Submission.Language language, String inputJson) {
        try {
            JsonNode inputNode = objectMapper.readTree(inputJson);
            
            // Use FunctionSignatureService to detect function name
            String functionName = functionSignatureService.detectFunctionName(userCode, language);
            
            // Validate the detected function name
            if (!functionSignatureService.isValidFunctionName(functionName)) {
                functionName = "solve"; // fallback to default
            }
            
            return switch (language) {
                case JAVASCRIPT -> createJavaScriptExecutionCode(userCode, inputNode, functionName);
                case PYTHON -> createPythonExecutionCode(userCode, inputNode, functionName);
                case JAVA -> createJavaExecutionCode(userCode, inputNode, functionName);
                case CPP -> createCppExecutionCode(userCode, inputNode, functionName);
                case C -> createCExecutionCode(userCode, inputNode, functionName);
                default -> throw new IllegalArgumentException("Unsupported language: " + language);
            };
        } catch (Exception e) {
            throw new RuntimeException("Failed to create execution code: " + e.getMessage(), e);
        }
    }

    private String createJavaScriptExecutionCode(String userCode, JsonNode inputNode, String functionName) {
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
        
        // Call function with direct parameters
        sb.append("const result = ").append(functionName).append("(");
        sb.append(String.join(", ", paramNames));
        sb.append(");\n");
        
        sb.append("console.log(JSON.stringify(result));");
        return sb.toString();
    }

    private String createPythonExecutionCode(String userCode, JsonNode inputNode, String functionName) {
    StringBuilder sb = new StringBuilder();
    sb.append(userCode).append("\n\n");
    sb.append("# Test execution\n");
    
    List<String> paramNames = new ArrayList<>();
    inputNode.fieldNames().forEachRemaining(fieldName -> {
        JsonNode fieldValue = inputNode.get(fieldName);
        sb.append(fieldName).append(" = ");
        
        if (fieldValue.isTextual()) {
            sb.append("\"").append(fieldValue.asText()).append("\"");
        } else if (fieldValue.isNumber()) {
            sb.append(fieldValue.asInt());  // Use asInt() for whole numbers
        } else if (fieldValue.isBoolean()) {
            sb.append(fieldValue.asBoolean());
        } else {
            sb.append(fieldValue.toString());
        }
        sb.append("\n");
        paramNames.add(fieldName);
    });
    
    // Check if it's a class method or function
    if (userCode.contains("class Solution")) {
        sb.append("solution = Solution()\n");
        sb.append("result = solution.").append(functionName).append("(");
    } else {
        sb.append("result = ").append(functionName).append("(");
    }
    
    sb.append(String.join(", ", paramNames));
    sb.append(")\n");
    
    sb.append("import json\n");
    sb.append("print(json.dumps(result))");
    return sb.toString();
}

    private String createJavaExecutionCode(String userCode, JsonNode inputNode, String functionName) {
        StringBuilder sb = new StringBuilder();
        sb.append("import java.util.*;\n");
        sb.append("import com.fasterxml.jackson.databind.ObjectMapper;\n\n");
        sb.append(userCode).append("\n\n");
        
        sb.append("public class Main {\n");
        sb.append("    public static void main(String[] args) throws Exception {\n");
        sb.append("        ObjectMapper mapper = new ObjectMapper();\n");
        sb.append("        Solution solution = new Solution();\n\n");
        
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
            } else if (fieldValue.isBoolean()) {
                sb.append("        boolean ").append(fieldName).append(" = ").append(fieldValue.asBoolean()).append(";\n");
                paramNames.add(fieldName);
            }
        });
        
        sb.append("\n");
        
        // Call user's function
        sb.append("        // Call user's solution\n");
        sb.append("        Object result = solution.").append(functionName).append("(");
        sb.append(String.join(", ", paramNames));
        sb.append(");\n\n");
        
        // Output the result
        sb.append("        // Output result as JSON\n");
        sb.append("        System.out.println(mapper.writeValueAsString(result));\n");
        sb.append("    }\n");
        sb.append("}");
        
        return sb.toString();
    }

    private String createCppExecutionCode(String userCode, JsonNode inputNode, String functionName) {
        StringBuilder sb = new StringBuilder();
        
        // Add necessary includes
        sb.append("#include <iostream>\n");
        sb.append("#include <vector>\n");
        sb.append("#include <string>\n");
        sb.append("#include <sstream>\n\n");
        
        // Add user's code
        sb.append(userCode).append("\n\n");
        
        // Create main function
        sb.append("int main() {\n");
        sb.append("    Solution solution;\n\n");
        
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
        sb.append("    auto result = solution.").append(functionName).append("(");
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

    private String createCExecutionCode(String userCode, JsonNode inputNode, String functionName) {
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
        sb.append("    int* result = ").append(functionName).append("(");
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

    private ExecutionResult executeInDocker(String executionCode, Submission.Language language, int timeLimitMs, int memoryLimitMb)
        throws IOException, InterruptedException {
    
    // Write code to temp dir
    Path tempDir = Files.createTempDirectory("submission_");
    Path codeFile = tempDir.resolve(getFileName(language));
    Files.writeString(codeFile, executionCode);

    String dockerImage = getDockerImage(language);
    String execCmd = getExecutionCommand(language);

    String dockerPath = tempDir.toAbsolutePath().toString().replace("\\", "/").replace("C:", "/c");
    String dockerCommand = String.format(
        "docker run --rm --memory=%dm --cpus=0.5 --user root -v \"%s:/code\" %s sh -c \"%s\"",
        memoryLimitMb, dockerPath, dockerImage, execCmd
    );

    long start = System.currentTimeMillis();
    Process process = new ProcessBuilder("bash", "-c", dockerCommand)
            .redirectErrorStream(true)
            .start();

    boolean finished = process.waitFor(timeLimitMs, TimeUnit.MILLISECONDS);
    long end = System.currentTimeMillis();
    int execTime = (int) (end - start);

    String output;
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
        output = reader.lines().collect(Collectors.joining("\n"));
    }

    String error = null;
    if (!finished) {
        process.destroyForcibly();
        return new ExecutionResult("", true, execTime, 0, "Time limit exceeded");
    }
    int exitCode = process.exitValue();
    if (exitCode != 0) {
        error = "Exit code: " + exitCode + "\n" + output;
    }

    deleteDirectory(tempDir);
    return new ExecutionResult(output.trim(), false, execTime, 0, error);
}




// Helper method to parse Java execution code into Solution.java and Main.java
private String[] parseJavaExecutionCode(String executionCode) {
    try {
        // Look for the Solution class
        int solutionStart = executionCode.indexOf("class Solution");
        int mainStart = executionCode.indexOf("public class Main");
        
        if (solutionStart >= 0 && mainStart >= 0) {
            String solutionCode = executionCode.substring(solutionStart, mainStart).trim();
            String mainCode = executionCode.substring(mainStart).trim();
            
            // Add necessary imports to both files
            String imports = "import java.util.*;\nimport com.fasterxml.jackson.databind.ObjectMapper;\n\n";
            
            return new String[] {
                imports + solutionCode,
                imports + mainCode
            };
        }
    } catch (Exception e) {
        System.out.println("Failed to parse Java execution code: " + e.getMessage());
    }
    
    // Return original code as single file if parsing fails
    return new String[] { executionCode };
}

    private String getExecutionCommand(Submission.Language language) {
    return switch (language) {
        case JAVASCRIPT -> "cd /code && node main.js";
        case PYTHON -> "cd /code && python main.py";  // Use 'python' instead of 'python3'
        case JAVA -> "cd /code && javac *.java && java Main";
        case CPP -> "cd /code && g++ -std=c++11 -o main main.cpp && ./main";
        case C -> "cd /code && gcc -o main main.c && ./main";
        default -> throw new IllegalArgumentException("Unsupported language: " + language);
    };
}

    private String getDockerImage(Submission.Language language) {
        return switch (language) {
            case JAVASCRIPT -> "node:18-slim";
            case JAVA -> "openjdk:17-slim";
            case PYTHON -> "python:3.9-slim";
            case CPP -> "gcc:latest";
            case C -> "gcc:latest";
            default -> throw new IllegalArgumentException("Unsupported language: " + language);
        };
    }

    private String getFileName(Submission.Language language) {
    return switch (language) {
        case JAVASCRIPT -> "main.js";
        case JAVA -> "Main.java"; // This is just for non-Java languages now
        case PYTHON -> "main.py";
        case CPP -> "main.cpp";
        case C -> "main.c";
        default -> throw new IllegalArgumentException("Unsupported language: " + language);
    };
}

    private String readStream(InputStream inputStream) throws IOException {
    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
    StringBuilder builder = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
        builder.append(line).append("\n");
    }
    return builder.toString();
}


    private String normalizeOutput(String output) {
        return output.trim().replaceAll("\\r\\n", "\n").replaceAll("\\s+", " ");
    }

    private void deleteDirectory(Path path) throws IOException {
        Files.walk(path)
            .sorted((a, b) -> -a.compareTo(b))
            .forEach(p -> {
                try { Files.delete(p); } 
                catch (IOException e) { /* Ignore */ }
            });
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

    // Existing methods
    public List<Submission> getSubmissionsByUserAndProblem(Long userId, Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("Problem not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return submissionRepository.findByUserAndProblem(user, problem);
    }

    public List<Submission> getSubmissionsByContestAndUser(Long contestId, Long userId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new IllegalArgumentException("Contest not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        List<ContestProblem> contestProblems = contestProblemRepository.findByContest(contest);
        List<Long> problemIds = contestProblems.stream()
                .map(cp -> cp.getProblem().getId())
                .collect(Collectors.toList());

        List<Submission> userSubmissions = submissionRepository.findByUser(user);
        return userSubmissions.stream()
                .filter(submission -> problemIds.contains(submission.getProblem().getId()))
                .collect(Collectors.toList());
    }

    public Long getTotalSubmissions() {
        return submissionRepository.count();
    }

    public Long getUserSubmissionCount(Long userId) {
        return (long) submissionRepository.findByUserId(userId).size();
    }

    public Long getUserAcceptedSubmissionCount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return submissionRepository.findByUser(user).stream()
                .filter(submission -> submission.getStatus() == Submission.Status.ACCEPTED)
                .count();
    }

    public Long getProblemSubmissionCount(Long problemId) {
        return (long) submissionRepository.findByProblemId(problemId).size();
    }

    public Long getProblemAcceptedSubmissionCount(Long problemId) {
        return submissionRepository.countAcceptedSubmissionsByProblemId(problemId);
    }

    public List<Submission> getRecentSubmissions() {
        return submissionRepository.findTop10ByOrderBySubmittedAtDesc();
    }

    public List<Submission> getSubmissionsWithFilters(String user, String problem, String status, String language) {
        return submissionRepository.findAll().stream()
                .filter(submission -> user == null || user.isEmpty() ||
                        submission.getUser().getUsername().toLowerCase().contains(user.toLowerCase()))
                .filter(submission -> problem == null || problem.isEmpty() ||
                        submission.getProblem().getTitle().toLowerCase().contains(problem.toLowerCase()))
                .filter(submission -> status == null || status.isEmpty() ||
                        submission.getStatus().name().equalsIgnoreCase(status))
                .filter(submission -> language == null || language.isEmpty() ||
                        submission.getLanguage().name().equalsIgnoreCase(language))
                .collect(Collectors.toList());
    }

    public String getSubmissionCode(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));
        return submission.getCode();
    }

    public Submission rerunSubmission(Long submissionId) {
        Submission submission = submissionRepository.findById(submissionId)
                .orElseThrow(() -> new IllegalArgumentException("Submission not found"));

        // Reset submission status
        submission.setStatus(Submission.Status.PENDING);
        submission.setErrorMessage(null);
        submission.setActualOutput(null);
        submission.setExpectedOutput(null);
        submission.setPassedTestCases(0);
        submission.setTotalTestCases(0);
        submission.setExecutionTime(0);
        submission.setMemoryUsed(0);

        submission = submissionRepository.save(submission);

        // Re-execute against test cases (ordered by ID)
        List<TestCase> testCases = testCaseRepository.findByProblemOrderByIdAsc(submission.getProblem());
        return executeAgainstTestCases(submission, submission.getProblem(), testCases);
    }

    public List<Submission> getRecentSubmissionsByUser(Long userId, int limit) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return submissionRepository.findByUserOrderBySubmittedAtDesc(user)
                .stream()
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Submission getSubmissionById(Long submissionId) {
        return submissionRepository.findById(submissionId).orElse(null);
    }
}