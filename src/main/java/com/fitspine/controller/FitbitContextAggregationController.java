package com.fitspine.controller;

import com.fitspine.dto.FitbitAiContextInsightDto;
import com.fitspine.service.FitbitContextAggregationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/wearable/fitbit")
public class FitbitContextAggregationController {
    private final FitbitContextAggregationService fitbitContextAggregationService;

    public FitbitContextAggregationController(FitbitContextAggregationService fitbitContextAggregationService) {
        this.fitbitContextAggregationService = fitbitContextAggregationService;
    }

    @GetMapping("/context")
    public ResponseEntity<FitbitAiContextInsightDto> getContext() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        LocalDate targetDate = LocalDate.now();
        System.out.println("Target Date:" + targetDate);
        FitbitAiContextInsightDto dto = fitbitContextAggregationService.buildContext(email, targetDate);
        return ResponseEntity.ok(dto);
    }
}
