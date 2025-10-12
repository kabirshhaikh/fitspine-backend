package com.fitspine.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FlareUpTriggersDto {
    private String metric;
    private String value;
    private String deviation;
    private String impact;
}
