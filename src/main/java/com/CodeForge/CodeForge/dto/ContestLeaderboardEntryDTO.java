package com.CodeForge.CodeForge.dto;

import com.CodeForge.CodeForge.model.ContestParticipant;
import com.CodeForge.CodeForge.model.User;

import java.time.LocalDateTime;

public class ContestLeaderboardEntryDTO {
    private Long id;
    private Long userId;
    private String username;
    private String email;
    private Integer score;
    private Integer rank;
    private Integer problemsSolved;
    private LocalDateTime joinedAt;

    public ContestLeaderboardEntryDTO() {}

    public ContestLeaderboardEntryDTO(ContestParticipant participant) {
        this.id = participant.getId();
        
        User user = participant.getUser();
        if (user != null) {
            this.userId = user.getId();
            this.username = user.getUsername();
            this.email = user.getEmail();
        }
        
        this.score = participant.getScore();
        this.rank = participant.getRank();
        this.joinedAt = participant.getJoinedAt();
        
        // Problems solved will be set separately
        this.problemsSolved = 0;
    }
    
    public ContestLeaderboardEntryDTO(ContestParticipant participant, int problemsSolved) {
        this(participant);
        this.problemsSolved = problemsSolved;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public Integer getRank() { return rank; }
    public void setRank(Integer rank) { this.rank = rank; }

    public Integer getProblemsSolved() { return problemsSolved; }
    public void setProblemsSolved(Integer problemsSolved) { this.problemsSolved = problemsSolved; }

    public LocalDateTime getJoinedAt() { return joinedAt; }
    public void setJoinedAt(LocalDateTime joinedAt) { this.joinedAt = joinedAt; }
}
