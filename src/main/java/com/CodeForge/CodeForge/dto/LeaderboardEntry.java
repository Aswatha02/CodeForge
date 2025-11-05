package com.CodeForge.CodeForge.dto;

public class LeaderboardEntry {
    private Integer rank;
    private Long userId;
    private String username;
    private Integer score;
    private Integer problemsSolved;
    private Long totalTime; // in seconds
    private Integer penalties;
    
    public LeaderboardEntry() {}
    
    public LeaderboardEntry(Integer rank, Long userId, String username, Integer score, 
                           Integer problemsSolved, Long totalTime, Integer penalties) {
        this.rank = rank;
        this.userId = userId;
        this.username = username;
        this.score = score;
        this.problemsSolved = problemsSolved;
        this.totalTime = totalTime;
        this.penalties = penalties;
    }
    
    // Getters and Setters
    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }
    
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }
    
    public Integer getProblemsSolved() { return problemsSolved; }
    public void setProblemsSolved(Integer problemsSolved) { this.problemsSolved = problemsSolved; }
    
    public Long getTotalTime() { return totalTime; }
    public void setTotalTime(Long totalTime) { this.totalTime = totalTime; }
    
    public Integer getPenalties() { return penalties; }
    public void setPenalties(Integer penalties) { this.penalties = penalties; }
}
