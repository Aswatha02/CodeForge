package com.CodeForge.CodeForge.services;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.CodeForge.CodeForge.model.*;
import com.CodeForge.CodeForge.repository.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;

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

    public SubmissionService(SubmissionRepository submissionRepository,
                           ProblemRepository problemRepository,
                           ContestRepository contestRepository,
                           UserRepository userRepository,
                           ContestProblemRepository contestProblemRepository,
                           ContestParticipantRepository contestParticipantRepository,
                           TestCaseRepository testCaseRepository,
                           LeaderboardService leaderboardService,
                           FunctionSignatureService functionSignatureService) {
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
        Submission submission = new Submission();
        submission.setUser(user);
        submission.setProblem(problem);
        submission.setCode(code);
        submission.setLanguage(language);
        submission.setStatus(Submission.Status.PENDING);
        submission.setSubmittedAt(LocalDateTime.now());
        
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
        // Use direct field access instead of getters
        submission.setTotalTestCases(testCases.size());
        submission.setPassedTestCases(0);

        boolean allPassed = true;
        int executionTime = 0;
        int memoryUsed = 0;

        for (int i = 0; i < testCases.size(); i++) {
            TestCase testCase = testCases.get(i);
            
            try {
                ExecutionResult result = executeSingleTestCase(
                    submission.getCode(), // This should work if you add getCode() to Submission model
                    submission.getLanguage(), // This should work if you add getLanguage() to Submission model
                    testCase.getInputData(),
                    problem.getTimeLimitMs(),
                    problem.getMemoryLimitMb()
                );

                executionTime = Math.max(executionTime, result.executionTime);
                memoryUsed = Math.max(memoryUsed, result.memoryUsed);

                if (result.timedOut) {
                    submission.setStatus(Submission.Status.TIME_LIMIT_EXCEEDED);
                    submission.setErrorMessage("Time limit exceeded on test case " + (i + 1));
                    return submission;
                }

                if (result.error != null) {
                    submission.setStatus(Submission.Status.RUNTIME_ERROR);
                    submission.setErrorMessage("Runtime error on test case " + (i + 1) + ": " + result.error);
                    return submission;
                }

                // Compare with expected output
                if (compareOutputs(result.output, testCase.getExpectedOutput())) {
                    submission.setPassedTestCases(submission.getPassedTestCases() + 1);
                } else {
                    submission.setStatus(Submission.Status.WRONG_ANSWER);
                    submission.setErrorMessage("Wrong answer on test case " + (i + 1));
                    submission.setActualOutput(result.output);
                    submission.setExpectedOutput(testCase.getExpectedOutput());
                    allPassed = false;
                    break;
                }

            } catch (Exception e) {
                submission.setStatus(Submission.Status.RUNTIME_ERROR);
                submission.setErrorMessage("Execution failed on test case " + (i + 1) + ": " + e.getMessage());
                return submission;
            }
        }

        submission.setExecutionTime(executionTime);
        submission.setMemoryUsed(memoryUsed);

        if (allPassed && submission.getPassedTestCases() == submission.getTotalTestCases()) {
            submission.setStatus(Submission.Status.ACCEPTED);
        }

        return submission;
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
            default -> throw new IllegalArgumentException("Unsupported language: " + language);
        };
    }

    private String getDockerImage(Submission.Language language) {
        return switch (language) {
            case JAVASCRIPT -> "node:18-slim";
            case JAVA -> "openjdk:17-slim";
            case PYTHON -> "python:3.9-slim";
            case CPP -> "gcc:latest";
            default -> throw new IllegalArgumentException("Unsupported language: " + language);
        };
    }

    private String getFileName(Submission.Language language) {
        return switch (language) {
            case JAVASCRIPT -> "main.js";
            case JAVA -> "Main.java";
            case PYTHON -> "main.py";
            case CPP -> "main.cpp";
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
}