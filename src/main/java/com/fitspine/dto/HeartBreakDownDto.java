package com.fitspine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HeartBreakDownDto {
    private String logDate;
    private Boolean isFitbitConnected;
    private Integer restingHeartRate;
    private String stressLevel;
}
