package com.CodeForge.CodeForge.services;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;  // For timeout
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
@Service
@jakarta.transaction.Transactional

public class SubmissionService
{
   @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private ContestRepository contestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ContestProblemRepository contestProblemRepository;

    @Autowired
    private ContestParticipantRepository contestParticipantRepository;

    @Autowired
    private TestCaseRepository testCaseRepository;

    @Autowired
    private LeaderboardService leaderboardService;

    @Transactional
    public Submission submitCode(Long problemId, Long contestId, User user, String code, Submission.Language language) {
        
        Problem problem = problemRepository.findById(problemId)
                .orElseThrow(() -> new IllegalArgumentException("Problem not found"));

        
        ContestParticipant participant = null;
        ContestProblem contestProblem = null;
        if (contestId != null) {
            Contest contest = contestRepository.findById(contestId)
                    .orElseThrow(() -> new IllegalArgumentException("Contest not found"));
            
            // Check if contest is running
            if (!contest.getStatus().equals(Contest.Status.RUNNING) ||
                contest.getStartTime().isAfter(LocalDateTime.now()) ||
                contest.getEndTime().isBefore(LocalDateTime.now())) {
                throw new IllegalStateException("Contest is not active");
            }

            // Check if problem is part of the contest
            contestProblem = contestProblemRepository.findByContestAndProblem(contest, problem)
                    .orElseThrow(() -> new IllegalArgumentException("Problem not in contest"));

            // Check if user is a participant
            participant = contestParticipantRepository.findByContestAndUser(contest, user)
                    .orElseThrow(() -> new IllegalStateException("User not registered for contest"));
        }

        // Create submission
        Submission submission = new Submission(user, problem, code, language);
        submission.setStatus(Submission.Status.PENDING);
        submission = submissionRepository.save(submission);

        // Execute code and evaluate
        submission = executeAndEvaluate(submission);

        // Update contest participant score if applicable
        if (contestId != null && submission.isAccepted()) {
            participant.setScore(participant.getScore() + contestProblem.getPoints());
            contestParticipantRepository.save(participant);
            leaderboardService.updateLeaderboard(contestId);
        }

        return submissionRepository.save(submission);
    }

    private Submission executeAndEvaluate(Submission submission) {
        List<TestCase> testCases = testCaseRepository.findByProblem(submission.getProblem());
        submission.setTotalTestCases(testCases.size());
        submission.setPassedTestCases(0);

        for (TestCase testCase : testCases) {
            try {
                String output = executeCode(submission.getCode(), submission.getLanguage(), testCase.getInputData());
                if (output.equals(testCase.getExpectedOutput())) {
                    submission.setPassedTestCases(submission.getPassedTestCases() + 1);
                } else {
                    submission.setStatus(Submission.Status.WRONG_ANSWER);
                    submission.setErrorMessage("Output mismatch for input: " + testCase.getInputData());
                    return submission;
                }
            } catch (RuntimeException e) {
                submission.setStatus(Submission.Status.RUNTIME_ERROR);
                submission.setErrorMessage(e.getMessage());
                return submission;
            } catch (Exception e) {
                submission.setStatus(Submission.Status.COMPILATION_ERROR);
                submission.setErrorMessage(e.getMessage());
                return submission;
            }
        }

        submission.setStatus(submission.getPassedTestCases() == submission.getTotalTestCases() 
            ? Submission.Status.ACCEPTED 
            : Submission.Status.WRONG_ANSWER);
        return submission;
    }

    private String executeCode(String code, Submission.Language language, String input) throws Exception {
    String extension;
    String[] command;
    switch (language) {
        case JAVA:
            extension = "java";
            // Wrap code in runnable class (assumes console I/O)
            code = "import java.util.Scanner;\npublic class Main {\npublic static void main(String[] args) {\nScanner sc = new Scanner(System.in);\n" + code + "\nsc.close();\n}\n}";
            command = new String[]{"javac", "Main.java", "&&", "java", "Main"};
            break;
        case PYTHON:
            extension = "py";
            command = new String[]{"python", "main.py"};
            break;
        case CPP:
            extension = "cpp";
            command = new String[]{"g++", "main.cpp", "-o", "main", "&&", "./main"};
            break;
        default:
            throw new IllegalArgumentException("Unsupported language: " + language);
    }

    // Temp files
    Path codeFile = Files.createTempFile("main", "." + extension);
    Files.write(codeFile, code.getBytes());
    Path inputFile = Files.createTempFile("input", ".txt");
    Files.write(inputFile, input.getBytes());

    // Run
    ProcessBuilder pb = new ProcessBuilder(command);
    pb.directory(codeFile.getParent().toFile());
    pb.redirectInput(inputFile.toFile());
    Process process = pb.start();

    // Capture stdout
    BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    StringBuilder output = new StringBuilder();
    String line;
    while ((line = reader.readLine()) != null) {
        output.append(line).append("\n");
    }

    // Capture stderr
    BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
    StringBuilder errors = new StringBuilder();
    while ((line = errorReader.readLine()) != null) {
        errors.append(line).append("\n");
    }

    // Timeout (2s)
    boolean finished = process.waitFor(2, TimeUnit.SECONDS);
    int exitCode = process.exitValue();

    // Cleanup
    Files.deleteIfExists(codeFile);
    Files.deleteIfExists(inputFile);

    if (!finished) {
        process.destroyForcibly();
        throw new RuntimeException("TIME_LIMIT_EXCEEDED");
    }

    if (exitCode != 0 || errors.length() > 0) {
        throw new Exception("COMPILATION_ERROR or RUNTIME_ERROR: " + errors.toString().trim());
    }

    return output.toString().trim();
}
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

        // Get all submissions by user and filter by contest problems
        List<Submission> userSubmissions = submissionRepository.findByUser(user);
        return userSubmissions.stream()
                .filter(submission -> problemIds.contains(submission.getProblem().getId()))
                .collect(Collectors.toList());
    }


}




