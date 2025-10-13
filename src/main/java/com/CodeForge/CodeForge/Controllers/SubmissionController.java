package com.CodeForge.CodeForge.controllers;

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
import com.CodeForge.CodeForge.dto.SubmissionRequestDTO;
import com.CodeForge.CodeForge.model.Submission;
import com.CodeForge.CodeForge.model.User;
import com.CodeForge.CodeForge.repository.UserRepository;
import com.CodeForge.CodeForge.services.SubmissionService;

@RestController
@RequestMapping("/api")
public class SubmissionController {

    @Autowired
    private SubmissionService submissionService;

    @Autowired
    private UserRepository userRepository;

    // Submit code for a regular problem (no contest)
    @PostMapping("/problems/{problemId}/submissions")
    public ResponseEntity<Submission> submitProblemCode(
            @PathVariable Long problemId,
            @RequestBody SubmissionRequestDTO dto,
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
                Submission.Language.valueOf(dto.getLanguage().toUpperCase())
        );

        return ResponseEntity.ok(submission);
    }

    // Submit code for a problem inside a contest
    @PostMapping("/contests/{contestId}/problems/{problemId}/submissions")
    public ResponseEntity<Submission> submitContestCode(
            @PathVariable Long contestId,
            @PathVariable Long problemId,
            @RequestBody SubmissionRequestDTO dto,
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
                Submission.Language.valueOf(dto.getLanguage().toUpperCase())
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
}

