package com.fitspine.service.impl;

import com.fitspine.dto.FitbitAiContextInsightDto;
import com.fitspine.service.FitbitContextAggregationService;

public class FitbitContextAggregationServiceImpl implements FitbitContextAggregationService {


    @Override
    public FitbitAiContextInsightDto buildContext() {
        FitbitAiContextInsightDto dto = new FitbitAiContextInsightDto();
        return dto;
    }
}
