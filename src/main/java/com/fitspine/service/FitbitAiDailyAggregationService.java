package com.fitspine.service;

import com.fitspine.dto.AiUserDailyInputDto;

import java.time.LocalDate;

public interface FitbitAiDailyAggregationService {
    AiUserDailyInputDto buildAiInput(String email, LocalDate logDate);
}
