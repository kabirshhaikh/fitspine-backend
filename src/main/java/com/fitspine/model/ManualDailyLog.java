package com.fitspine.model;

import com.fitspine.enums.*;
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
@Table(name = "manual_daily_log")
public class ManualDailyLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "log_date", nullable = false)
    private LocalDate logDate;

    @Column(name = "pain_level")
    @Enumerated(EnumType.STRING)
    private PainLevel painLevel;

    @Column(name = "flare_up_today")
    private Boolean flareUpToday;

    @Column(name = "numbness_tingling")
    private Boolean numbnessTingling;

    @Column(name = "sitting_time")
    @Enumerated(EnumType.STRING)
    private SittingTime sittingTime;

    @Column(name = "standing_time")
    @Enumerated(EnumType.STRING)
    private StandingTime standingTime;

    @Column(name = "stretching_done")
    private Boolean stretchingDone;

    @Column(name = "morning_stiffness")
    @Enumerated(EnumType.STRING)
    private MorningStiffness morningStiffness;

    @Column(name = "stress_level")
    @Enumerated(EnumType.STRING)
    private StressLevel stressLevel;

    @Column(name = "lifting_or_strain")
    private Boolean liftingOrStrain;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @OneToMany(mappedBy = "manualDailyLog", cascade = CascadeType.ALL, orphanRemoval = true)
    List<ManualDailyPainLocationLog> manualDailyPainLocationLogs = new ArrayList<>();

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
