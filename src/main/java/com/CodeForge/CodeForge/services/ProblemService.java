package com.CodeForge.CodeForge.services;

import com.CodeForge.CodeForge.model.*;
import com.CodeForge.CodeForge.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class ProblemService {
    
    private final ProblemRepository problemRepository;
    private final CategoryRepository categoryRepository;
    private final TestCaseRepository testCaseRepository;
    private final UserRepository userRepository;
    private final SubmissionRepository submissionRepository;
    private final UserService userService;

    @Transactional
    public Problem createProblem(Problem problem, Long creatorId) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new RuntimeException("Creator not found"));
        
        // Check if slug already exists
        if (problemRepository.findBySlug(problem.getSlug()).isPresent()) {
            throw new RuntimeException("Problem slug already exists");
        }
        
        problem.setCreator(creator);
        
        // Save problem first to get ID
        Problem savedProblem = problemRepository.save(problem);
        
        // Save test cases
        if (problem.getTestCases() != null) {
            for (TestCase testCase : problem.getTestCases()) {
                testCase.setProblem(savedProblem);
                testCaseRepository.save(testCase);
            }
        }
        
        return savedProblem;
    }

    public List<Problem> getAllProblems() {
        return problemRepository.findAllActive();
    }

    public Optional<Problem> getProblemById(Long id) {
        // CHANGED: Use simple findById instead of complex join
        return problemRepository.findById(id);
    }

    public Optional<Problem> getProblemBySlug(String slug) {
        return problemRepository.findBySlug(slug);
    }

    public List<Problem> getProblemsByCategory(Long categoryId) {
        return problemRepository.findByCategoryId(categoryId);
    }

    public List<Problem> getProblemsByDifficulty(Problem.Difficulty difficulty) {
        return problemRepository.findByDifficulty(difficulty);
    }

    @Transactional
    public Submission submitSolution(Long problemId, Long userId, String code, Submission.Language language) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new RuntimeException("Problem not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        
        Submission submission = new Submission();
        submission.setProblem(problem);
        submission.setUser(user);
        submission.setCode(code);
        submission.setLanguage(language);
        submission.setStatus(Submission.Status.PENDING);
        
        // Get test cases for this problem
        List<TestCase> testCases = testCaseRepository.findByProblemId(problemId);
        
        // Actually execute and test the code
        Submission result = executeAndTestCode(submission, testCases);
        
        Submission savedSubmission = submissionRepository.save(result);
        
        // Update user progress
        boolean accepted = result.getStatus() == Submission.Status.ACCEPTED;
        userService.updateProgressAfterSubmission(userId, accepted, problem.getDifficulty().name());        
        return savedSubmission;
    }

    private Submission executeAndTestCode(Submission submission, List<TestCase> testCases) {
        if (testCases.isEmpty()) {
            submission.setStatus(Submission.Status.ACCEPTED); // No test cases = auto accept
            submission.setExecutionTime(0);
            submission.setMemoryUsed(0);
            return submission;
        }

        // For Python submissions
        if (submission.getLanguage() == Submission.Language.PYTHON) {
            return executePythonCode(submission, testCases);
        }

        // For other languages, you can add similar methods
        submission.setStatus(Submission.Status.RUNTIME_ERROR);
        submission.setExecutionTime(0);
        submission.setMemoryUsed(0);
        return submission;
    }

    private Submission executePythonCode(Submission submission, List<TestCase> testCases) {
        try {
            // Create a temporary Python file
            File pythonFile = File.createTempFile("submission", ".py");
            pythonFile.deleteOnExit();
            
            // Write the user's code to the file
            try (FileWriter writer = new FileWriter(pythonFile)) {
                writer.write(submission.getCode());
                writer.write("\n\n");
                writer.write(getPythonTestRunner());
            }

            boolean allTestsPassed = true;
            int totalExecutionTime = 0;
            int maxMemoryUsed = 0;

            for (TestCase testCase : testCases) {
                ProcessBuilder processBuilder = new ProcessBuilder("python", pythonFile.getAbsolutePath());
                processBuilder.redirectErrorStream(true);
                
                Process process = processBuilder.start();
                
                // Write input to process
                try (OutputStream outputStream = process.getOutputStream();
                     PrintWriter writer = new PrintWriter(outputStream)) {
                    writer.write(testCase.getInputData());
                    writer.flush();
                }
                
                // Wait for process to complete with timeout (5 seconds)
                boolean finished = process.waitFor(5, TimeUnit.SECONDS);
                
                if (!finished) {
                    process.destroyForcibly();
                    submission.setStatus(Submission.Status.TIME_LIMIT_EXCEEDED);
                    submission.setExecutionTime(5000); // 5 seconds timeout
                    submission.setMemoryUsed(0);
                    return submission;
                }
                
                // Read output
                StringBuilder output = new StringBuilder();
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        output.append(line).append("\n");
                    }
                }
                
                String actualOutput = output.toString().trim();
                String expectedOutput = testCase.getExpectedOutput().trim();
                
                // Compare outputs
                if (!actualOutput.equals(expectedOutput)) {
                    allTestsPassed = false;
                    break;
                }
                
                // Simulate execution metrics (in real system, you'd use proper profiling)
                totalExecutionTime += 50 + (int)(Math.random() * 50); // 50-100ms per test
                maxMemoryUsed = Math.max(maxMemoryUsed, 5 + (int)(Math.random() * 5)); // 5-10MB
            }
            
            pythonFile.delete();
            
            if (allTestsPassed) {
                submission.setStatus(Submission.Status.ACCEPTED);
            } else {
                submission.setStatus(Submission.Status.WRONG_ANSWER);
            }
            
            submission.setExecutionTime(totalExecutionTime);
            submission.setMemoryUsed(maxMemoryUsed);
            
        } catch (IOException e) {
            submission.setStatus(Submission.Status.RUNTIME_ERROR);
            submission.setExecutionTime(0);
            submission.setMemoryUsed(0);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            submission.setStatus(Submission.Status.RUNTIME_ERROR);
            submission.setExecutionTime(0);
            submission.setMemoryUsed(0);
        } catch (Exception e) {
            submission.setStatus(Submission.Status.RUNTIME_ERROR);
            submission.setExecutionTime(0);
            submission.setMemoryUsed(0);
        }
        
        return submission;
    }

    private String getPythonTestRunner() {
        return """
            import sys
            import ast
            
            def read_input():
                return sys.stdin.read().strip()
            
            if __name__ == "__main__":
                try:
                    input_data = read_input()
                    result = twoSum(input_data)
                    if result is None:
                        print("[]")
                    elif isinstance(result, list):
                        print(str(result))
                    else:
                        print(str(result))
                except Exception as e:
                    print("ERROR: " + str(e))
                    sys.exit(1)
            """;
    }

    @Transactional
    public void deleteProblem(Long problemId) {
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new RuntimeException("Problem not found"));
        
        // Delete related test cases first
        testCaseRepository.deleteByProblemId(problemId);
        
        problemRepository.delete(problem);
    }

    public List<Problem> searchProblems(String query, String difficulty, Long categoryId) {
        // Basic search implementation - can be enhanced with full-text search
        List<Problem> allProblems = getAllProblems();
        
        return allProblems.stream()
                .filter(problem -> 
                    (query == null || query.isEmpty() || 
                     problem.getTitle().toLowerCase().contains(query.toLowerCase()) ||
                     problem.getDescription().toLowerCase().contains(query.toLowerCase())) &&
                    (difficulty == null || difficulty.isEmpty() || 
                     problem.getDifficulty().name().equalsIgnoreCase(difficulty)) &&
                    (categoryId == null || 
                     problem.getCategories().stream().anyMatch(cat -> cat.getId().equals(categoryId)))
                )
                .toList();
    }
}