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
@Table(name = "fitbit_sleep_log")
public class FitbitSleepLog {
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

    @Column(name = "date_of_sleep")
    private LocalDate dateOfSleep;

    @Column(name = "efficiency")
    private Integer efficiency;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "info_code")
    private Integer infoCode;

    @Column(name = "is_main_sleep")
    private Boolean isMainSleep;

    @Column(name = "log_id")
    private Long logId;

    @Column(name = "minutes_after_wakeup")
    private Integer minutesAfterWakeup;

    @Column(name = "minutes_awake")
    private Integer minutesAwake;

    @Column(name = "minutes_asleep")
    private Integer minutesAsleep;

    @Column(name = "minutes_to_fall_asleep")
    private Integer minutesToFallAsleep;

    @Column(name = "log_type")
    private String logType;

    @Column(name = "time_in_bed")
    private Integer timeInBed;

    @Column(name = "type")
    private String type;

    @OneToMany(mappedBy = "fitbitSleepLog", cascade = CascadeType.ALL, orphanRemoval = true)
    List<FitbitSleepDataLog> fitbitSleepDataLogs = new ArrayList<>();

    @OneToMany(mappedBy = "fitbitSleepLog", cascade = CascadeType.ALL, orphanRemoval = true)
    List<FitbitSleepShortDataLog> fitbitSleepShortDataLogs = new ArrayList<>();

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
