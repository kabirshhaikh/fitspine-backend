package com.fitspine.service;

import com.fitspine.dto.AiUserDailyInputDto;

import java.time.LocalDate;

public interface AiDailyAggregationService {
    AiUserDailyInputDto buildAiInput(String email, LocalDate logDate);
}
