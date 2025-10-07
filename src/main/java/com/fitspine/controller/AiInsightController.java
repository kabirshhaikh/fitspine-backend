package com.fitspine.controller;

import com.fitspine.dto.AiInsightResponseDto;
import com.fitspine.dto.AiUserDailyInputDto;
import com.fitspine.service.AiDailyFitbitAggregationService;
import com.fitspine.service.AiInsightService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/insights")
public class AiInsightController {
    private final AiInsightService aiInsightService;
    private final AiDailyFitbitAggregationService aiDailyAggregationService;

    public AiInsightController(AiInsightService aiInsightService, AiDailyFitbitAggregationService aiDailyAggregationService) {
        this.aiInsightService = aiInsightService;
        this.aiDailyAggregationService = aiDailyAggregationService;
    }

    @GetMapping("/generate/{date}")
    public ResponseEntity<AiInsightResponseDto> generateDailyInsight(@PathVariable LocalDate date) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();

        AiUserDailyInputDto dto = aiDailyAggregationService.buildAiInput(email, date);
        log.info("User daily input dto {}", dto);
        AiInsightResponseDto responseDto = aiInsightService.generateDailyInsight(dto, email, date);
        return ResponseEntity.ok(responseDto);
    }
}
