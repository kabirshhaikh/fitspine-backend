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
@Table(name = "fitbit_activities_heart_values_heart_rate_zones_log")
@EntityListeners(EntityAuditListener.class)

public class FitbitActivitiesHeartValueHeartRateZonesLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_fitbit_activities_heart_values_log", nullable = false)
    private FitbitActivitiesHeartValueLog fitbitActivitiesHeartValuesLog;

    @Column(name = "calories_out")
    private Double caloriesOut;

    @Column(name = "max")
    private Integer max;

    @Column(name = "min")
    private Integer min;

    @Column(name = "minutes")
    private Integer minutes;

    @Column(name = "name")
    private String name;

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
