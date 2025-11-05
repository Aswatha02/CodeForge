package com.CodeForge.CodeForge.services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.CodeForge.CodeForge.dto.LeaderboardEntryDTO;
import com.CodeForge.CodeForge.model.Contest;
import com.CodeForge.CodeForge.model.ContestParticipant;
import com.CodeForge.CodeForge.model.User;
import com.CodeForge.CodeForge.model.UserProgress;
import com.CodeForge.CodeForge.repository.ContestParticipantRepository;
import com.CodeForge.CodeForge.repository.ContestRepository;
import com.CodeForge.CodeForge.repository.UserProgressRepository;
import com.CodeForge.CodeForge.repository.UserRepository;

@Service
@Transactional
public class LeaderboardService {

    private final ContestParticipantRepository contestParticipantRepository;
    private final ContestRepository contestRepository;
    private final UserRepository userRepository;
    private final UserProgressRepository userProgressRepository;

    @Autowired
    public LeaderboardService(ContestParticipantRepository contestParticipantRepository,
                             ContestRepository contestRepository,
                             UserRepository userRepository,
                             UserProgressRepository userProgressRepository) {
        this.contestParticipantRepository = contestParticipantRepository;
        this.contestRepository = contestRepository;
        this.userRepository = userRepository;
        this.userProgressRepository = userProgressRepository;
    }

    /**
     * Get global leaderboard based on user progress
     * @return List of leaderboard entries sorted by total score descending
     */
    public List<LeaderboardEntryDTO> getGlobalLeaderboard() {
        List<UserProgress> allProgress = userProgressRepository.findAllWithUser();
        
        return allProgress.stream()
                .map(progress -> new LeaderboardEntryDTO(progress.getUser(), progress))
                .sorted((a, b) -> b.getTotalScore().compareTo(a.getTotalScore()))
                .collect(Collectors.toList());
    }

    /**
     * Get complete leaderboard for a contest, sorted by score descending
     * @param contestId ID of the contest
     * @return List of participation records ordered by score (highest first)
     */
    public List<ContestParticipant> getLeaderboard(Long contestId) {

        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new RuntimeException("Contest not found with id: " + contestId));
        // Use findByContestWithUser to eagerly fetch user data for JSON serialization
        List<ContestParticipant> participants = contestParticipantRepository.findByContestWithUser(contest);
        // Sort by score descending
        participants.sort((a, b) -> b.getScore().compareTo(a.getScore()));
        return participants;
    }


    /**
     * Refresh and recalculate the leaderboard for a contest
     * @param contestId ID of the contest to update
     */
    public void updateLeaderboard(Long contestId) {
        // Get the contest
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new RuntimeException("Contest not found with id: " + contestId));
        
        // Get all participations for this contest
        List<ContestParticipant> participations = contestParticipantRepository.findByContest(contest);
        
        // Recalculate scores for each participant
        for (ContestParticipant participation : participations) {
            int newScore = calculateUserScore(participation.getUser(), contest);
            participation.setScore(newScore);
            contestParticipantRepository.save(participation);
        }
        
        // Update ranks after recalculating scores
        updateRanks(contestId);
    }

    /**
     * Update ranks based on current scores
     * @param contestId ID of the contest
     */
    private void updateRanks(Long contestId) {
        List<ContestParticipant> leaderboard = getLeaderboard(contestId);
        
        for (int i = 0; i < leaderboard.size(); i++) {
            ContestParticipant participant = leaderboard.get(i);
            participant.setRank(i + 1); // 1-based ranking
            contestParticipantRepository.save(participant);
        }
    }

    /**
     * Get a specific user's rank in the contest
     * @param contestId ID of the contest
     * @param userId ID of the user
     * @return The user's rank (1 = first place) or -1 if not found
     */
    public int getUserRank(Long contestId, Long userId) {
        // Get the full leaderboard
        List<ContestParticipant> leaderboard = getLeaderboard(contestId);
        
        // Find the user's position (1-based indexing)
        for (int i = 0; i < leaderboard.size(); i++) {
            if (leaderboard.get(i).getUser().getId().equals(userId)) {
                return i + 1; // 1st place = index 0 + 1
            }
        }
        
        return -1; // User not found in leaderboard
    }

    /**
     * Get paginated leaderboard for better performance with large contests
     * @param contestId ID of the contest
     * @param pageable Pagination information (page number, page size)
     * @return Page of leaderboard entries
     */
    public List<ContestParticipant> getPaginatedLeaderboard(Long contestId) {
        if (!contestRepository.existsById(contestId)) {
            throw new RuntimeException("Contest not found with id: " + contestId);
        }
        
       Contest contest = contestRepository.findById(contestId)
        .orElseThrow(() -> new RuntimeException("Contest not found"));

        return contestParticipantRepository.findByContestOrderByScoreDesc(contest);

    }

    /**
     * Get top N performers in a contest
     * @param contestId ID of the contest
     * @param limit Number of top performers to return
     * @return List of top participants
     */
    public List<ContestParticipant> getTopPerformers(Long contestId, int limit) {
        List<ContestParticipant> leaderboard = getLeaderboard(contestId);
        return leaderboard.stream()
                .limit(limit)
                .toList();
    }

    public ContestParticipant getUserRankInContest(Long contestId, Long userId) {
    List<ContestParticipant> leaderboard = getLeaderboard(contestId);
    for (ContestParticipant cp : leaderboard) {
        if (cp.getUser().getId().equals(userId)) {
            return cp;
        }
    }
    return null;
    }


    /**
     * Helper method to calculate a user's score for a contest
     * This is where your scoring logic goes
     * @param user The user
     * @param contest The contest
     * @return Calculated score
     */
    private int calculateUserScore(User user, Contest contest) {
        // TODO: Implement your actual scoring logic here
        // This will depend on your Submission model and business rules
        
        // Example placeholder logic:
        int baseScore = 100;
        int solvedProblems = 5; // This should come from your database
        int timeBonus = 50; // This should be calculated from submission times
        
        return baseScore + (solvedProblems * 100) + timeBonus;
    }

    /**
     * Reset all scores for a contest (useful for testing)
     * @param contestId ID of the contest
     */
    public void resetLeaderboard(Long contestId) {
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new RuntimeException("Contest not found with id: " + contestId));
        
        List<ContestParticipant> participations = contestParticipantRepository.findByContest(contest);
        
        for (ContestParticipant participation : participations) {
            participation.setScore(0);
            participation.setRank(0);
            contestParticipantRepository.save(participation);
        }
    }

    /**
     * Get leaderboard with user details eagerly fetched
     * @param contestId ID of the contest
     * @return List of participants with user information
     */
    public List<ContestParticipant> getLeaderboardWithUsers(Long contestId) {
        if (!contestRepository.existsById(contestId)) {
            throw new RuntimeException("Contest not found with id: " + contestId);
        }
        Contest contest = contestRepository.findById(contestId)
                .orElseThrow(() -> new RuntimeException("Contest not found with id: " + contestId));

        return contestParticipantRepository.findByContestWithUser(contest);
    }
}