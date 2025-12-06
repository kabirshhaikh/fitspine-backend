package com.fitspine.model;

import com.fitspine.listener.EntityAuditListener;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Table(name = "ai_daily_insights_risk_forecasts")
@EntityListeners(EntityAuditListener.class)

public class AiDailyInsightRiskForecasts {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_ai_daily_insights", nullable = false)
    private AiDailyInsight aiDailyInsight;

    @Column(name = "flareup_risk_score")
    private Integer flareUpRiskScore;

    @Column(name = "pain_risk_score")
    private Integer painRiskScore;

    @Column(name = "risk_bucket")
    private String riskBucket;

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
