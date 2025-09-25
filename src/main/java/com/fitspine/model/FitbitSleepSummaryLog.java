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
@Table(name = "fitbit_sleep_summaries_log")
public class FitbitSleepSummaryLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "provider", nullable = false)
    private String provider;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "total_minutes_asleep")
    private Integer totalMinutesAsleep;

    @Column(name = "total_sleep_records")
    private Integer totalSleepRecords;

    @Column(name = "total_time_in_bed")
    private Integer totalTimeInBed;

    @Lob
    @Column(name = "raw_json", columnDefinition = "LONGTEXT")
    private String rawJson;

    @OneToOne(mappedBy = "fitbitSleepSummaryLog", cascade = CascadeType.ALL, orphanRemoval = true)
    private FitbitSleepSummaryStagesLog fitbitSleepSummaryStagesLogs;

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
