package com.fitspine.controller;

import com.fitspine.service.AiDailyFitbitAggregationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/insights")
public class AiDailyAggregationController {
    private final AiDailyFitbitAggregationService aiDailyAggregationService;

    public AiDailyAggregationController(AiDailyFitbitAggregationService aiDailyAggregationService) {
        this.aiDailyAggregationService = aiDailyAggregationService;
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<?> getAiInput(@PathVariable LocalDate date) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return ResponseEntity.ok(aiDailyAggregationService.buildAiInput(email, date));
    }
}
