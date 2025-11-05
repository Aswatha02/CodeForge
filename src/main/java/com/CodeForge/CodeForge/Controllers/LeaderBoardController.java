package com.CodeForge.CodeForge.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.CodeForge.CodeForge.Exception.ContestNotFoundException;
import com.CodeForge.CodeForge.dto.ContestLeaderboardEntryDTO;
import com.CodeForge.CodeForge.dto.LeaderboardEntryDTO;
import com.CodeForge.CodeForge.model.Contest;
import com.CodeForge.CodeForge.model.ContestParticipant;
import com.CodeForge.CodeForge.model.Submission;
import com.CodeForge.CodeForge.model.User;
import com.CodeForge.CodeForge.repository.SubmissionRepository;
import com.CodeForge.CodeForge.services.ContestService;
import com.CodeForge.CodeForge.services.LeaderboardService;

@RestController
@RequestMapping("/api")
public class LeaderBoardController {

    @Autowired
    private LeaderboardService leaderboardService;

    @Autowired
    private SubmissionRepository submissionRepository;

    @Autowired
    private ContestService contestService;

    // Global leaderboard endpoint
    @GetMapping("/leaderboard")
    public ResponseEntity<List<LeaderboardEntryDTO>> getGlobalLeaderboard(
            @AuthenticationPrincipal User user) {
        try {
            List<LeaderboardEntryDTO> leaderboard = leaderboardService.getGlobalLeaderboard();
            return ResponseEntity.ok(leaderboard);
        } catch (Exception e) {
            return ResponseEntity.ok(List.of()); // Return empty list on error
        }
    }

    @GetMapping("/contests/{contestId}/leaderboard")
    public ResponseEntity<List<ContestLeaderboardEntryDTO>> getLeaderBoard(
            @PathVariable("contestId") Long contestId,
            @AuthenticationPrincipal User user) {

        // Allow all authenticated users to view leaderboard
        try {
            Contest contest = contestService.getContest(contestId);
            List<ContestParticipant> participants = leaderboardService.getLeaderboard(contestId);
            
            // Get all problems in this contest
            java.util.List<com.CodeForge.CodeForge.model.ContestProblem> contestProblems = 
                contestService.getContestProblems(contestId);
            java.util.Set<Long> contestProblemIds = contestProblems.stream()
                    .map(cp -> cp.getProblem().getId())
                    .collect(java.util.stream.Collectors.toSet());
            
            List<ContestLeaderboardEntryDTO> leaderboard = participants.stream()
                    .map(participant -> {
                        // Count unique problems solved by this user in this contest
                        List<Submission> submissions = submissionRepository.findByUserId(participant.getUser().getId());
                        
                        // Get accepted submissions during contest time for contest problems
                        java.util.List<Submission> acceptedSubmissions = submissions.stream()
                                .filter(s -> contestProblemIds.contains(s.getProblem().getId()))
                                .filter(s -> {
                                    // Check if submission was made during contest time
                                    if (contest.getStartTime() != null && contest.getEndTime() != null) {
                                        return !s.getSubmittedAt().isBefore(contest.getStartTime()) 
                                            && !s.getSubmittedAt().isAfter(contest.getEndTime());
                                    }
                                    return true; // If no time constraints, allow all
                                })
                                .filter(s -> s.getStatus() == Submission.Status.ACCEPTED)
                                .collect(java.util.stream.Collectors.toList());
                        
                        // Count unique problems solved
                        long problemsSolved = acceptedSubmissions.stream()
                                .map(s -> s.getProblem().getId())
                                .distinct()
                                .count();
                        
                        // Calculate score based on contest problem points
                        int totalScore = acceptedSubmissions.stream()
                                .map(s -> s.getProblem().getId())
                                .distinct()
                                .mapToInt(problemId -> {
                                    // Find the points for this problem in the contest
                                    return contestProblems.stream()
                                            .filter(cp -> cp.getProblem().getId().equals(problemId))
                                            .findFirst()
                                            .map(cp -> cp.getPoints() != null ? cp.getPoints() : 100)
                                            .orElse(100);
                                })
                                .sum();
                        
                        ContestLeaderboardEntryDTO dto = new ContestLeaderboardEntryDTO(participant, (int) problemsSolved);
                        dto.setScore(totalScore); // Override with calculated score
                        return dto;
                    })
                    .collect(java.util.stream.Collectors.toList());
            
            return ResponseEntity.ok(leaderboard);
        } catch (Exception e) {
            throw new ContestNotFoundException(contestId);
        }
    }

    @GetMapping("/contests/{contestId}/leaderboard/{userId}")
    public ResponseEntity<ContestParticipant> getUserRankInContest(
            @PathVariable("contestId") Long contestId,
            @PathVariable("userId") Long userId,
            @AuthenticationPrincipal User user) {

        // Allow all authenticated users to view user rank
        try {
            ContestParticipant participant = leaderboardService.getUserRankInContest(contestId, userId);
            if (participant == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(participant);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}
