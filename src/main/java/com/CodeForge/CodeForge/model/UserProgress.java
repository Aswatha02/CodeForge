package com.CodeForge.CodeForge.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "user_progress")
public class UserProgress {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ToString.Exclude
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "solved_count", nullable = false)
    private Integer solvedCount = 0;

    @Column(name = "solved_easy_count", nullable = false)
    private Integer solvedEasyCount = 0;

    @Column(name = "solved_medium_count", nullable = false)
    private Integer solvedMediumCount = 0;

    @Column(name = "solved_hard_count", nullable = false)
    private Integer solvedHardCount = 0;

    @Column(name = "total_submissions", nullable = false)
    private Integer totalSubmissions = 0;

    @Column(name = "accepted_submissions", nullable = false)
    private Integer acceptedSubmissions = 0;

    @Column(name = "current_streak", nullable = false)
    private Integer currentStreak = 0;

    @Column(name = "max_streak", nullable = false)
    private Integer maxStreak = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.ACTIVE;

    public enum Status {
        ACTIVE, INACTIVE
    }
}