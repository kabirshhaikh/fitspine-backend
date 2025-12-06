package com.fitspine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskForecastDto {
    private Integer flareUpRiskScore;
    private Integer painRiskScore;

    private String riskBucket;
}
