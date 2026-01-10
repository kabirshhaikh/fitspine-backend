package com.fitspine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityBreakDownDto {
    private String logDate;
    private String standingTime;
    private String sittingTime;
    private Boolean isFitbitConnected;
    private Double fitbitSedentaryHours;
}
