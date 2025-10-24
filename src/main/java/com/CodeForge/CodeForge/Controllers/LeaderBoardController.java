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
import com.CodeForge.CodeForge.model.ContestParticipant;
import com.CodeForge.CodeForge.model.User;
import com.CodeForge.CodeForge.services.LeaderboardService;

@RestController
@RequestMapping("/api")
public class LeaderBoardController {

    @Autowired
    private LeaderboardService leaderboardService;

    @GetMapping("/contests/{contestId}/leaderboard")
    public ResponseEntity<List<ContestParticipant>> getLeaderBoard(
            @PathVariable("contestId") Long contestId,
            @AuthenticationPrincipal User user) {

        // Allow all authenticated users to view leaderboard
        try {
            List<ContestParticipant> leaderboard = leaderboardService.getLeaderboard(contestId);
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
