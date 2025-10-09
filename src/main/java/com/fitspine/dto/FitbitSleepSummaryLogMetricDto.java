package com.fitspine.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FitbitSleepSummaryLogMetricDto {
    private Integer totalMinutesAsleep;
}
