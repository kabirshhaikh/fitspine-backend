package com.fitspine.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FitbitActivitySummariesMetricDto {
    private Integer caloriesOut;
    private Integer steps;
    private Integer sedentaryMinutes;
}
