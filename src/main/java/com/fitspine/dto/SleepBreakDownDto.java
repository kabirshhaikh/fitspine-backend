package com.fitspine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SleepBreakDownDto {
    private String logDate;
    private String timeAsleep;
    private String nightWakeUps;
    private Boolean isFitbitConnected;
}

