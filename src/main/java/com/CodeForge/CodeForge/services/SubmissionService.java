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

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.CodeForge.CodeForge.dto.ExecutionRequest;
import com.CodeForge.CodeForge.dto.ExecutionResponse;
import com.CodeForge.CodeForge.model.Contest;
import com.CodeForge.CodeForge.model.ContestParticipant;
import com.CodeForge.CodeForge.model.ContestProblem;
import com.CodeForge.CodeForge.model.Problem;
import com.CodeForge.CodeForge.model.Submission;
import com.CodeForge.CodeForge.model.TestCase;
import com.CodeForge.CodeForge.model.User;
import com.CodeForge.CodeForge.repository.ContestParticipantRepository;
import com.CodeForge.CodeForge.repository.ContestProblemRepository;
import com.CodeForge.CodeForge.repository.ContestRepository;
import com.CodeForge.CodeForge.repository.ProblemRepository;
import com.CodeForge.CodeForge.repository.SubmissionRepository;
import com.CodeForge.CodeForge.repository.TestCaseRepository;
import com.CodeForge.CodeForge.repository.UserRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

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
            // Get all test cases for the problem
            List<TestCase> testCases = testCaseRepository.findByProblem(problem);
            
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
        // Create ExecutionRequest
        ExecutionRequest request = new ExecutionRequest();
        request.setCombinedCode(submission.getCode());
        request.setLanguage(submission.getLanguage().name());
        request.setTimeLimitMs(problem.getTimeLimitMs());
        request.setMemoryLimitMb(problem.getMemoryLimitMb());

        // Convert test cases to ExecutionRequest.TestCaseExecution
        List<ExecutionRequest.TestCaseExecution> testCaseExecutions = testCases.stream()
            .map(tc -> {
                ExecutionRequest.TestCaseExecution tce = new ExecutionRequest.TestCaseExecution();
                tce.setTestCaseId(tc.getId());
                tce.setInputData(tc.getInputData());
                tce.setExpectedOutput(tc.getExpectedOutput());
                tce.setWeight(1); // Default weight
                return tce;
            })
            .collect(Collectors.toList());

        request.setTestCases(testCaseExecutions);

        // Execute using ExecutionService
        ExecutionResponse response = executionService.executeCode(request);

        // Process the response
        submission.setTotalTestCases(testCases.size());
        submission.setPassedTestCases(0);

        if ("SUCCESS".equals(response.getStatus())) {
            // Count passed test cases
            int passedCount = 0;
            for (ExecutionResponse.TestCaseExecutionResult result : response.getTestCaseResults()) {
                if ("PASSED".equals(result.getStatus())) {
                    passedCount++;
                } else if ("FAILED".equals(result.getStatus())) {
                    // Set wrong answer details from the first failed test case
                    if (submission.getStatus() != Submission.Status.WRONG_ANSWER) {
                        submission.setStatus(Submission.Status.WRONG_ANSWER);
                        submission.setActualOutput(result.getActualOutput());
                        // Find the corresponding test case to get expected output
                        TestCase failedTestCase = testCases.stream()
                            .filter(tc -> tc.getId().equals(result.getTestCaseId()))
                            .findFirst().orElse(null);
                        if (failedTestCase != null) {
                            submission.setExpectedOutput(failedTestCase.getExpectedOutput());
                        }
                        submission.setErrorMessage("Wrong answer on test case");
                    }
                } else if ("TIME_LIMIT_EXCEEDED".equals(result.getStatus())) {
                    submission.setStatus(Submission.Status.TIME_LIMIT_EXCEEDED);
                    submission.setErrorMessage("Time limit exceeded");
                    return submission;
                } else if ("RUNTIME_ERROR".equals(result.getStatus())) {
                    submission.setStatus(Submission.Status.RUNTIME_ERROR);
                    submission.setErrorMessage("Runtime error: " + result.getErrorMessage());
                    return submission;
                }
            }
            submission.setPassedTestCases(passedCount);

            if (passedCount == testCases.size()) {
                submission.setStatus(Submission.Status.ACCEPTED);
            }

            submission.setExecutionTime(response.getTotalExecutionTime() != null ? response.getTotalExecutionTime() : 0);
            submission.setMemoryUsed(response.getMaxMemoryUsed() != null ? response.getMaxMemoryUsed() / 1024 : 0); // Convert KB to MB

        } else if ("COMPILATION_ERROR".equals(response.getStatus())) {
            submission.setStatus(Submission.Status.COMPILATION_ERROR);
            submission.setErrorMessage("Compilation error: " + response.getCompilationError());
        } else if ("EXECUTION_SERVICE_ERROR".equals(response.getStatus()) || "EXECUTION_SERVICE_UNAVAILABLE".equals(response.getStatus())) {
            submission.setStatus(Submission.Status.RUNTIME_ERROR);
            submission.setErrorMessage("Execution service error: " + response.getCompilationError());
        } else {
            submission.setStatus(Submission.Status.RUNTIME_ERROR);
            submission.setErrorMessage("Unknown execution error");
        }

        return submission;
    }

    private boolean compareOutputs(String actual, String expected) {
        try {
            // Parse both outputs as JSON for flexible comparison
            JsonNode actualNode = objectMapper.readTree(actual.trim());
            JsonNode expectedNode = objectMapper.readTree(expected.trim());

            return actualNode.equals(expectedNode);

        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            // If JSON parsing fails, do string comparison (normalized)
            return normalizeOutput(actual).equals(normalizeOutput(expected));
        }
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

    private ExecutionResult executeInDocker(String executionCode, Submission.Language language,
                                          int timeLimitMs, int memoryLimitMb) throws Exception {
        String imageName = getDockerImage(language);
        String containerName = "submission-" + System.currentTimeMillis();

        // Create temporary files
        Path codeDir = Files.createTempDirectory("code");
        Path codeFile = codeDir.resolve(getFileName(language));
        
        Files.write(codeFile, executionCode.getBytes());

        try {
            // Docker run command with resource limits
            ProcessBuilder pb = new ProcessBuilder(
                "docker", "run", "--rm",
                "--name", containerName,
                "--memory", memoryLimitMb + "m",
                "--memory-swap", memoryLimitMb + "m",
                "--cpus", "0.5",
                "--network", "none",
                "--read-only",
                "-v", codeDir.toString() + ":/code:ro",
                imageName,
                getExecutionCommand(language)
            );

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

    private String getExecutionCommand(Submission.Language language) {
        return switch (language) {
            case JAVASCRIPT -> "node /code/main.js";
            case PYTHON -> "python /code/main.py";
            case JAVA -> "cd /code && javac Main.java && java Main";
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
            case JAVA -> "Main.java";
            case PYTHON -> "main.py";
            case CPP -> "main.cpp";
            case C -> "main.c";
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

        // Re-execute against test cases
        List<TestCase> testCases = testCaseRepository.findByProblem(submission.getProblem());
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
}