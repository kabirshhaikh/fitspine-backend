package com.fitspine.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RiskForecastDto {
    private Double risk;
    private String bucket;
}
