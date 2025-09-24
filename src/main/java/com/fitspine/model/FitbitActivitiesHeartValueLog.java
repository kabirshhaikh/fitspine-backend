package com.fitspine.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Table(name = "fitbit_activities_heart_values_log")
public class FitbitActivitiesHeartValueLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_fitbit_activities_heart_log", nullable = false)
    private FitbitActivitiesHeartLog fitbitActivitiesHeartLog;

    @Column(name = "resting_heart_rate")
    private Integer restingHeartRate;

    @OneToMany(mappedBy = "fitbitActivitiesHeartValuesLog", cascade = CascadeType.ALL, orphanRemoval = true)
    List<FitbitActivitiesHeartValueHeartRateZonesLog> heartRateZonesLogs = new ArrayList<>();

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
