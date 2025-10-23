package com.fitspine.service;

import com.fitspine.dto.FitbitAiContextInsightDto;
import com.fitspine.dto.WeeklyGraphDto;
import com.fitspine.model.User;

import java.time.LocalDate;

public interface FitbitContextAggregationService {
    FitbitAiContextInsightDto buildContext(String email, LocalDate targetDate); //Here target date is current date

    WeeklyGraphDto generateWeeklyGraph(LocalDate targetDate, User user);
}
