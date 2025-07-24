package com.fitspine.model;

import com.fitspine.enums.Gender;
import com.fitspine.enums.WearableType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false, length = 255)
    private String fullName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password", nullable = false, length = 100)
    private String password;

    @Column(name = "age", nullable = false)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Column(name = "profile_picture")
    private String profilePicture;

    @Column(name = "surgery_history", nullable = false)
    private Boolean surgeryHistory = false;

    @Column(name = "is_research_opt", nullable = false)
    private Boolean isResearchOpt = false;

    @Column(name = "is_wearable_connected")
    private Boolean isWearableConnected = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "wearable_type")
    private WearableType wearableType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

}
