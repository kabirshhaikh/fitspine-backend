package com.fitspine.service;

import com.fitspine.dto.FitbitAiContextInsightDto;

import java.time.LocalDate;

public interface FitbitContextAggregationService {
    FitbitAiContextInsightDto buildContext(String email, LocalDate targetDate); //Here target date is current date
}
