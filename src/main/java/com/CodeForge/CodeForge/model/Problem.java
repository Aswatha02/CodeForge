package com.codeforge.codeforge.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "problems")
public class Problem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String title;

    @NotBlank
    @Column(unique = true, nullable = false)
    private String slug;

    @NotBlank
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Difficulty difficulty;

    @Column(columnDefinition = "TEXT")
    private String constraints;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "acceptance_rate")
    private Double acceptanceRate = 0.0;

    @Column(name = "submission_count")
    private Integer submissionCount = 0;

    @Column(name = "accepted_count")
    private Integer acceptedCount = 0;

    private Integer likes = 0;
    private Integer dislikes = 0;

    @Enumerated(EnumType.STRING)
    private Status status = Status.DRAFT;

    // Add this field - Many-to-Many relationship with Category
    @ManyToMany
    @JoinTable(
        name = "problem_categories",
        joinColumns = @JoinColumn(name = "problem_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    // JPA Lifecycle callback for updatedAt
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Constructors
    public Problem() {}

    public Problem(String title, String slug, String description, Difficulty difficulty, User createdBy) {
        this.title = title;
        this.slug = slug;
        this.description = description;
        this.difficulty = difficulty;
        this.createdBy = createdBy;
    }

    // Enums
    public enum Difficulty { EASY, MEDIUM, HARD }
    public enum Status { ACTIVE, DRAFT, ARCHIVED }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Difficulty getDifficulty() { return difficulty; }
    public void setDifficulty(Difficulty difficulty) { this.difficulty = difficulty; }

    public String getConstraints() { return constraints; }
    public void setConstraints(String constraints) { this.constraints = constraints; }

    public User getCreatedBy() { return createdBy; }
    public void setCreatedBy(User createdBy) { this.createdBy = createdBy; }

    public Double getAcceptanceRate() { return acceptanceRate; }
    public void setAcceptanceRate(Double acceptanceRate) { this.acceptanceRate = acceptanceRate; }

    public Integer getSubmissionCount() { return submissionCount; }
    public void setSubmissionCount(Integer submissionCount) { this.submissionCount = submissionCount; }

    public Integer getAcceptedCount() { return acceptedCount; }
    public void setAcceptedCount(Integer acceptedCount) { this.acceptedCount = acceptedCount; }

    public Integer getLikes() { return likes; }
    public void setLikes(Integer likes) { this.likes = likes; }

    public Integer getDislikes() { return dislikes; }
    public void setDislikes(Integer dislikes) { this.dislikes = dislikes; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    // Add categories getter and setter
    public List<Category> getCategories() {
        return categories;
    }

    public void setCategories(List<Category> categories) {
        this.categories = categories;
    }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    // Helper methods
    public void incrementSubmissionCount() {
        this.submissionCount++;
        updateAcceptanceRate();
    }

    public void incrementAcceptedCount() {
        this.acceptedCount++;
        updateAcceptanceRate();
    }

    public void incrementLikes() { this.likes++; }
    public void incrementDislikes() { this.dislikes++; }

    // Add these helper methods for bidirectional relationship management
    public void addCategory(Category category) {
        categories.add(category);
        category.getProblems().add(this);
    }

    public void removeCategory(Category category) {
        categories.remove(category);
        category.getProblems().remove(this);
    }

    public int getCategoryCount() {
        return categories != null ? categories.size() : 0;
    }

    private void updateAcceptanceRate() {
        if (submissionCount > 0) {
            this.acceptanceRate = (acceptedCount * 100.0) / submissionCount;
        }
    }

    public boolean isActive() {
        return this.status == Status.ACTIVE;
    }

    @Override
    public String toString() {
        return "Problem{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", slug='" + slug + '\'' +
                ", difficulty=" + difficulty +
                ", status=" + status +
                ", categoryCount=" + getCategoryCount() +
                '}';
    }
}