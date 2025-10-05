package com.CodeForge.CodeForge.Controllers;

import com.CodeForge.CodeForge.model.Problem;
import com.CodeForge.CodeForge.model.Submission;
import com.CodeForge.CodeForge.services.ProblemService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class ProblemController {
    
    private final ProblemService problemService;

    @PostMapping
    public ResponseEntity<?> createProblem(
            @RequestBody Problem problem,
            @RequestParam Long creatorId) {
        try {
            Problem createdProblem = problemService.createProblem(problem, creatorId);
            return ResponseEntity.ok(createdProblem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<List<Problem>> getAllProblems() {
        List<Problem> problems = problemService.getAllProblems();
        return ResponseEntity.ok(problems);
    }

    @GetMapping("/search")
    public ResponseEntity<List<Problem>> searchProblems(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) Long categoryId) {
        List<Problem> problems = problemService.searchProblems(query, difficulty, categoryId);
        return ResponseEntity.ok(problems);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProblemById(@PathVariable Long id) {
        try {
            Problem problem = problemService.getProblemById(id)
                    .orElseThrow(() -> new RuntimeException("Problem not found"));
            return ResponseEntity.ok(problem);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/{problemId}/submit")
    public ResponseEntity<?> submitSolution(
            @PathVariable Long problemId,
            @RequestBody Map<String, Object> submissionRequest) {
        try {
            Long userId = Long.valueOf(submissionRequest.get("userId").toString());
            String code = (String) submissionRequest.get("code");
            Submission.Language language = Submission.Language.valueOf(
                submissionRequest.get("language").toString().toUpperCase());
            
            Submission submission = problemService.submitSolution(problemId, userId, code, language);
            return ResponseEntity.ok(submission);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid submission data"));
        }
    }

    // Admin endpoints
    @DeleteMapping("/admin/{problemId}")
    public ResponseEntity<?> deleteProblem(@PathVariable Long problemId) {
        try {
            problemService.deleteProblem(problemId);
            return ResponseEntity.ok().body(Map.of("message", "Problem deleted successfully"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }
}