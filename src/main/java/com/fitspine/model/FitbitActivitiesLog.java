package com.fitspine.model;

import com.fitspine.listener.EntityAuditListener;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Table(name = "fitbit_activities_log")
@EntityListeners(EntityAuditListener.class)

public class FitbitActivitiesLog {
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

    @Column(name = "log_id")
    private Long logId;

    @Column(name = "activity_id")
    private Integer activityId;

    @Column(name = "activity_parent_id")
    private Integer activityParentId;


    @Column(name = "activity_parent_name")
    private String activityParentName;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "calories")
    private Integer calories;

    @Column(name = "distance")
    private Double distance;

    @Column(name = "steps")
    private Integer steps;

    @Column(name = "duration")
    private Long duration;

    @Column(name = "last_modified")
    private LocalDateTime lastModified;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "is_favourite")
    private Boolean favourite;

    @Column(name = "has_active_zone_minutes")
    private Boolean hasActiveZoneMinutes;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "has_start_time")
    private Boolean hasStartTime;

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
