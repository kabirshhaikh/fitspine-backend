package com.fitspine.controller;

import com.fitspine.dto.DashboardInsightDto;
import com.fitspine.dto.WeeklyGraphDto;
import com.fitspine.service.AiInsightService;
import com.fitspine.service.DashboardCalculationService;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Response;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@RequestMapping("/api/insights/dashboard-insights")
public class DashboardController {
    private final DashboardCalculationService dashboardService;
    private final AiInsightService aiInsightService;


    public DashboardController(
            DashboardCalculationService dashboardService,
            AiInsightService aiInsightService
    ) {
        this.dashboardService = dashboardService;
        this.aiInsightService = aiInsightService;
    }

    @GetMapping("/weekly-graph/{date}")
    public ResponseEntity<DashboardInsightDto> getDashboardInsight(@PathVariable LocalDate date) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        WeeklyGraphDto weeklyGraph = aiInsightService.weeklyGraph(date, email);
        DashboardInsightDto weeklyDashBoardInsight = dashboardService.calculate(weeklyGraph);
        return ResponseEntity.ok(weeklyDashBoardInsight);
    }
}
