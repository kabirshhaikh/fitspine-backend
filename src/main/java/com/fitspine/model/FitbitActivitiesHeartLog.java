package com.fitspine.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Table(name = "fitbit_activities_heart_log")
public class FitbitActivitiesHeartLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, name = "provider")
    private String provider;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "date_time")
    private LocalDateTime dateTime;

    @Lob
    @Column(name = "raw_json")
    private String rawJson;

    @OneToMany(mappedBy = "fitbitActivitiesHeartLog", cascade = CascadeType.ALL, orphanRemoval = true)
    List<FitbitActivitiesHeartValueLog> values = new ArrayList<>();

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
