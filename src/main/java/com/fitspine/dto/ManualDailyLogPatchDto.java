package com.fitspine.dto;

import com.fitspine.enums.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class ManualDailyLogPatchDto {
    private LocalDate logDate;
    private PainLevel painLevel;
    private Boolean flareUpToday;
    private Boolean numbnessTingling;
    private SittingTime sittingTime;
    private StandingTime standingTime;
    private Boolean stretchingDone;
    private MorningStiffness morningStiffness;
    private StressLevel stressLevel;
    private Boolean liftingOrStrain;
    private String notes;
    private SleepDuration sleepDuration;
    private NightWakeUps nightWakeUps;

    @Min(value = 30, message = "Resting heart rate must be at least 30 bpm")
    @Max(value = 120, message = "Resting heart rate must be 120 bpm or less")
    private Integer restingHeartRate;

    private List<PainLocation> painLocations;
}
