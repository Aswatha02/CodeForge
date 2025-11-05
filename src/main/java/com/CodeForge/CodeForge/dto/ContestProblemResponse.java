package com.CodeForge.CodeForge.dto;

public class ContestProblemResponse {
    private Long id;
    private String title;
    private String difficulty;
    private Integer points;
    private Integer totalSubmissions;
    private Integer acceptedSubmissions;
    private Boolean solved; // Has current user solved it?
    private Integer userAttempts;
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    
    public Integer getTotalSubmissions() { return totalSubmissions; }
    public void setTotalSubmissions(Integer totalSubmissions) { this.totalSubmissions = totalSubmissions; }
    
    public Integer getAcceptedSubmissions() { return acceptedSubmissions; }
    public void setAcceptedSubmissions(Integer acceptedSubmissions) { this.acceptedSubmissions = acceptedSubmissions; }
    
    public Boolean getSolved() { return solved; }
    public void setSolved(Boolean solved) { this.solved = solved; }
    
    public Integer getUserAttempts() { return userAttempts; }
    public void setUserAttempts(Integer userAttempts) { this.userAttempts = userAttempts; }
}
