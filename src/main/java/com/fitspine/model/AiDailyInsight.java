package com.fitspine.model;

import com.fitspine.enums.WearableType;
import com.fitspine.listener.EntityAuditListener;
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
@Table(name = "ai_daily_insights")
@EntityListeners(EntityAuditListener.class)

public class AiDailyInsight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "provider")
    @Enumerated(EnumType.STRING)
    private WearableType provider;

    @Column(name = "todays_insights", columnDefinition = "TEXT")
    private String todaysInsights;

    @Column(name = "recovery_insights", columnDefinition = "TEXT")
    private String recoveryInsights;

    @Column(name = "disc_score_explanation", columnDefinition = "TEXT")
    private String discScoreExplanation;

    @Column(name = "disc_protection_score")
    private Integer discProtectionScore;

    @Column(name = "model_used")
    private String modelUsed;

    @Column(name = "total_tokens")
    private Integer totalTokens;

    @Column(name = "prompt_tokens")
    private Integer promptTokens;

    @Column(name = "completion_tokens")
    private Integer completionTokens;

    @OneToMany(mappedBy = "aiDailyInsight", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AiDailyInsightFlareUpTriggers> flareUpTriggers = new ArrayList<>();

    @OneToMany(mappedBy = "aiDailyInsight", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AiDailyInsightInterventionsToday> interventionsToday = new ArrayList<>();

    @OneToMany(mappedBy = "aiDailyInsight", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AiDailyInsightActionableAdvice> actionableAdvices = new ArrayList<>();

    @OneToMany(mappedBy = "aiDailyInsight", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AiDailyInsightPossibleCauses> possibleCausesList = new ArrayList<>();

    @OneToOne(mappedBy = "aiDailyInsight", cascade = CascadeType.ALL, orphanRemoval = true)
    private AiDailyInsightRiskForecasts riskForecasts;

    @OneToMany(mappedBy = "aiDailyInsight", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AiDailyInsightImproved> improved = new ArrayList<>();

    @OneToMany(mappedBy = "aiDailyInsight", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AiDailyInsightWorsened> worsened = new ArrayList<>();

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
