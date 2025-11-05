package com.CodeForge.CodeForge.dto;

import java.util.List;

public class ContestDashboardResponse {
    private ContestResponse contest;
    private List<ContestProblemResponse> problems;
    private LeaderboardEntry userRank;
    private List<LeaderboardEntry> topRanks;
    private ContestStats stats;
    
    public static class ContestStats {
        private Long totalParticipants;
        private Long totalSubmissions;
        private Integer averageScore;
        private Long timeElapsed; // seconds
        private Long timeRemaining; // seconds
        
        // Getters and Setters
        public Long getTotalParticipants() { return totalParticipants; }
        public void setTotalParticipants(Long totalParticipants) { this.totalParticipants = totalParticipants; }
        
        public Long getTotalSubmissions() { return totalSubmissions; }
        public void setTotalSubmissions(Long totalSubmissions) { this.totalSubmissions = totalSubmissions; }
        
        public Integer getAverageScore() { return averageScore; }
        public void setAverageScore(Integer averageScore) { this.averageScore = averageScore; }
        
        public Long getTimeElapsed() { return timeElapsed; }
        public void setTimeElapsed(Long timeElapsed) { this.timeElapsed = timeElapsed; }
        
        public Long getTimeRemaining() { return timeRemaining; }
        public void setTimeRemaining(Long timeRemaining) { this.timeRemaining = timeRemaining; }
    }
    
    // Getters and Setters
    public ContestResponse getContest() { return contest; }
    public void setContest(ContestResponse contest) { this.contest = contest; }
    
    public List<ContestProblemResponse> getProblems() { return problems; }
    public void setProblems(List<ContestProblemResponse> problems) { this.problems = problems; }
    
    public LeaderboardEntry getUserRank() { return userRank; }
    public void setUserRank(LeaderboardEntry userRank) { this.userRank = userRank; }
    
    public List<LeaderboardEntry> getTopRanks() { return topRanks; }
    public void setTopRanks(List<LeaderboardEntry> topRanks) { this.topRanks = topRanks; }
    
    public ContestStats getStats() { return stats; }
    public void setStats(ContestStats stats) { this.stats = stats; }
}
