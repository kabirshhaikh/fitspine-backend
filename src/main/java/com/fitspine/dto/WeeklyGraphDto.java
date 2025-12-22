package com.fitspine.dto;

import com.fitspine.model.UserDiscIssue;
import com.fitspine.model.UserInjury;
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
    private Boolean isFitbitConnected;
    List<DailyGraphDto> dailyData;

    //User disc issues:
    List<String> userDiscIssues;

    //User injuries:
    List<String> userInjuryList;
}
