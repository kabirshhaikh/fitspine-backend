package com.fitspine.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeeklyGraphDto {
    private List<String> dates;
    private List<Integer> painLevel;
    private List<Integer> sittingTime;
    private List<Integer> standingTime;
    private List<Integer> morningStiffness;
    private List<Integer> stressLevel;
    private List<Integer> restingHeartRate;
    private List<Double> sedentaryHours;
}
