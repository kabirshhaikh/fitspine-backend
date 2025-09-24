package com.fitspine.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Table(name = "fitbit_activity_summaries_log")
public class FitbitActivitySummariesLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "provider", nullable = false)
    private String provider;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "calories_out")
    private Integer caloriesOut;

    @Column(name = "activity_calories")
    private Integer activityCalories;

    @Column(name = "calories_bmr")
    private Integer caloriesBmr;

    @Column(name = "active_score")
    private Integer activeScore;

    @Column(name = "steps")
    private Integer steps;

    @Column(name = "sedentary_minutes")
    private Integer sedentaryMinutes;

    @Column(name = "lightly_active_minutes")
    private Integer lightlyActiveMinutes;

    @Column(name = "fairly_active_minutes")
    private Integer fairlyActiveMinutes;

    @Column(name = "very_active_minutes")
    private Integer veryActiveMinutes;

    @Column(name = "marginal_calories")
    private Integer marginalCalories;

    @Lob
    @Column(name = "raw_json", columnDefinition = "LONGTEXT")
    private String rawJson;

    @OneToMany(mappedBy = "fitbitActivitySummariesLog", cascade = CascadeType.ALL, orphanRemoval = true)
    List<FitbitActivitySummariesDistancesLog> fitbitActivitySummariesDistancesLogs = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
