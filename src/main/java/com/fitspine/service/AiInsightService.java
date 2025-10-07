package com.fitspine.service;

import com.fitspine.dto.AiInsightResponseDto;
import com.fitspine.dto.AiUserDailyInputDto;

import java.time.LocalDate;

public interface AiInsightService {
    AiInsightResponseDto generateDailyInsight(AiUserDailyInputDto dto, String email, LocalDate logDate);
}
