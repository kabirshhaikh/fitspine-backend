package com.fitspine.dto;

import com.fitspine.enums.*;
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

    private List<PainLocation> painLocations;
}
