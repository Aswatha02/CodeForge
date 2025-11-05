package com.CodeForge.CodeForge.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.CodeForge.CodeForge.model.Contest;

public class ContestResponse {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer duration;
    private Boolean isPublic;
    private String status;
    private Integer maxParticipants;
    private Long participantCount;
    private Integer problemCount;
    private String createdBy;
    private LocalDateTime createdAt;
    private Boolean isRegistered;
    private Long timeRemaining; // seconds until start/end
    
    // Constructor from Contest entity
    public ContestResponse(Contest contest) {
        this.id = contest.getId();
        this.title = contest.getTitle();
        this.description = contest.getDescription();
        this.startTime = contest.getStartTime();
        this.endTime = contest.getEndTime();
        this.duration = contest.getDuration();
        this.isPublic = contest.getIsPublic();
        this.status = contest.getStatus().name();
        this.maxParticipants = contest.getMaxParticipants();
        this.createdBy = contest.getCreatedBy() != null ? contest.getCreatedBy().getUsername() : null;
        this.createdAt = contest.getCreatedAt();
    }
    
    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
    
    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
    
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    
    public Boolean getIsPublic() { return isPublic; }
    public void setIsPublic(Boolean isPublic) { this.isPublic = isPublic; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public Integer getMaxParticipants() { return maxParticipants; }
    public void setMaxParticipants(Integer maxParticipants) { this.maxParticipants = maxParticipants; }
    
    public Long getParticipantCount() { return participantCount; }
    public void setParticipantCount(Long participantCount) { this.participantCount = participantCount; }
    
    public Integer getProblemCount() { return problemCount; }
    public void setProblemCount(Integer problemCount) { this.problemCount = problemCount; }
    
    public String getCreatedBy() { return createdBy; }
    public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public Boolean getIsRegistered() { return isRegistered; }
    public void setIsRegistered(Boolean isRegistered) { this.isRegistered = isRegistered; }
    
    public Long getTimeRemaining() { return timeRemaining; }
    public void setTimeRemaining(Long timeRemaining) { this.timeRemaining = timeRemaining; }
}
