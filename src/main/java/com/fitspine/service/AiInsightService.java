package com.fitspine.service;

import com.fitspine.dto.AiInsightResponseDto;
import com.fitspine.dto.AiUserDailyInputDto;

public interface AiInsightService {
    AiInsightResponseDto generateDailyInsight(AiUserDailyInputDto dto);
}
