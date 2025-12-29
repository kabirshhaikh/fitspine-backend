package com.fitspine.service;

import com.fitspine.dto.AiInsightResponseDto;
import com.fitspine.dto.AiUserDailyInputDto;
import com.fitspine.dto.WeeklyGraphDto;

import java.time.LocalDate;

public interface AiInsightService {
    AiInsightResponseDto generateDailyInsight(AiUserDailyInputDto dto, String email, LocalDate logDate);

    WeeklyGraphDto weeklyGraph(LocalDate date, String email);

    AiInsightResponseDto getAiInsightForDay(LocalDate date, String email);
}
