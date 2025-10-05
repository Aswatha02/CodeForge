package com.CodeForge.CodeForge.Controllers;

import com.CodeForge.CodeForge.model.Submission;
import com.CodeForge.CodeForge.repository.SubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/submissions")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class SubmissionController {
    
    private final SubmissionRepository submissionRepository;

    @GetMapping
    public ResponseEntity<List<Submission>> getSubmissions(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Long problemId) {
        
        List<Submission> submissions;
        
        if (userId != null && problemId != null) {
            submissions = submissionRepository.findByUserIdAndProblemId(userId, problemId);
        } else if (userId != null) {
            submissions = submissionRepository.findByUserIdWithProblem(userId);
        } else if (problemId != null) {
            submissions = submissionRepository.findByProblemId(problemId);
        } else {
            submissions = submissionRepository.findAll();
        }
        
        return ResponseEntity.ok(submissions);
    }

    @GetMapping("/admin/stats")
    public ResponseEntity<Map<String, Object>> getSystemStats() {
        Long totalSubmissions = submissionRepository.count();
        Long totalAccepted = submissionRepository.countTotalAcceptedSubmissions();
        Long activeUsers = submissionRepository.countActiveUsers();
        Long totalProblems = 75L; // This should come from ProblemRepository
        
        double acceptanceRate = totalSubmissions > 0 ? 
            (double) totalAccepted / totalSubmissions * 100 : 0;
        
        return ResponseEntity.ok(Map.of(
            "totalSubmissions", totalSubmissions,
            "totalAccepted", totalAccepted,
            "activeUsers", activeUsers,
            "totalProblems", totalProblems,
            "acceptanceRate", Math.round(acceptanceRate * 100.0) / 100.0
        ));
    }
}