package com.CodeForge.CodeForge.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.ToString;

@Data
@Entity
@Table(name = "test_cases")
public class TestCase {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Input data is required")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String inputData;

    @NotBlank(message = "Expected output is required")
    @Column(columnDefinition = "TEXT", nullable = false)
    private String expectedOutput;

    @Column(name = "is_sample", nullable = false)
    private Boolean isSample = true;

    @JsonIgnore
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private Problem problem;
}