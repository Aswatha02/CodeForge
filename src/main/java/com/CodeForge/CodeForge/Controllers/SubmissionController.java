package com.CodeForge.CodeForge.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.CodeForge.CodeForge.Exception.UserNotAuthorizedException;
import com.CodeForge.CodeForge.dto.SubmissionRequest;
import com.CodeForge.CodeForge.model.Submission;
import com.CodeForge.CodeForge.model.User;
import com.CodeForge.CodeForge.repository.UserRepository;
import com.CodeForge.CodeForge.services.SubmissionService;

@RestController
@RequestMapping("/api")
public class SubmissionController {

    private final SubmissionService submissionService;

    private final UserRepository userRepository;

    @Autowired
    public SubmissionController(SubmissionService submissionService, UserRepository userRepository) {
        this.submissionService = submissionService;
        this.userRepository = userRepository;
    }

    // Submit code for a regular problem (no contest)
    @PostMapping("/problems/{problemId}/submissions")
    public ResponseEntity<Submission> submitProblemCode(
            @PathVariable Long problemId,
            @RequestBody SubmissionRequest dto,
            java.security.Principal principal) {

        if (principal == null) throw new UserNotAuthorizedException("submit code");
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotAuthorizedException("submit code"));

        Submission submission = submissionService.submitCode(
                problemId,
                null,
                user,
                dto.getCode(),
                dto.getLanguage()
        );
                

        return ResponseEntity.ok(submission);
    }

    // Submit code for a problem inside a contest
    @PostMapping("/contests/{contestId}/problems/{problemId}/submissions")
    public ResponseEntity<Submission> submitContestCode(
            @PathVariable Long contestId,
            @PathVariable Long problemId,
            @RequestBody SubmissionRequest dto,
            java.security.Principal principal) {

        if (principal == null) throw new UserNotAuthorizedException("submit code");
        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotAuthorizedException("submit code"));

        Submission submission = submissionService.submitCode(
                problemId,
                contestId,
                user,
                dto.getCode(),
                dto.getLanguage()
        );

        return ResponseEntity.ok(submission);
    }

    // Get user's submissions for a problem
    @GetMapping("/users/{userId}/problems/{problemId}/submissions")
    public ResponseEntity<List<Submission>> getSubmissionsByUserAndProblem(
            @PathVariable Long userId,
            @PathVariable Long problemId) {

        List<Submission> submissions = submissionService.getSubmissionsByUserAndProblem(userId, problemId);
        return ResponseEntity.ok(submissions);
    }

    // Get user's submissions in a contest
    @GetMapping("/contests/{contestId}/users/{userId}/submissions")
    public ResponseEntity<List<Submission>> getSubmissionsByContestAndUser(
            @PathVariable Long contestId,
            @PathVariable Long userId) {

        List<Submission> submissions = submissionService.getSubmissionsByContestAndUser(contestId, userId);
        return ResponseEntity.ok(submissions);
    }

    // Rerun a submission (users can only rerun their own submissions)
    @PostMapping("/submissions/{submissionId}/rerun")
    public ResponseEntity<?> rerunSubmission(@PathVariable Long submissionId, java.security.Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Authentication required");
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotAuthorizedException("rerun submission"));

        try {
            // Verify the submission belongs to the authenticated user
            Submission submission = submissionService.getSubmissionById(submissionId);
            if (submission == null) {
                return ResponseEntity.notFound().build();
            }

            if (!submission.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("You can only rerun your own submissions");
            }

            // Rerun the submission
            Submission rerunResult = submissionService.rerunSubmission(submissionId);
            return ResponseEntity.ok(rerunResult);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to rerun submission: " + e.getMessage());
        }
    }

    // Get submission code (users can only view their own submission code)
    @GetMapping("/submissions/{submissionId}/code")
    public ResponseEntity<String> getSubmissionCode(@PathVariable Long submissionId, java.security.Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Authentication required");
        }

        String username = principal.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotAuthorizedException("get submission code"));

        try {
            // Verify the submission belongs to the authenticated user
            Submission submission = submissionService.getSubmissionById(submissionId);
            if (submission == null) {
                return ResponseEntity.notFound().build();
            }

            if (!submission.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("You can only view your own submission code");
            }

            // Get the submission code
            String code = submissionService.getSubmissionCode(submissionId);
            return ResponseEntity.ok(code);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to get submission code: " + e.getMessage());
        }
    }
}

