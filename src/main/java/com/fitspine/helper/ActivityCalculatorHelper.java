package com.fitspine.helper;

import com.fitspine.dto.DailyGraphDto;
import com.fitspine.dto.DaySummaryDto;
import com.fitspine.dto.ExplanationDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class ActivityCalculatorHelper {
    public List<DailyGraphDto> getLoggedDaysWithActivityData(List<DailyGraphDto> allDays) {
        List<DailyGraphDto> loggedDays = new ArrayList<>();

        if (allDays == null || allDays.isEmpty()) {
            return loggedDays;
        }

        for (int i = 0; i < allDays.size(); i++) {
            DailyGraphDto day = allDays.get(i);
            if (day == null) continue;

            if ((day.getStandingTime() != null && day.getStandingTime() != -1) && (day.getSittingTime() != null && day.getSittingTime() != -1)) {
                loggedDays.add(day);
            }
        }

        return loggedDays;
    }

    public Double calculateActivityBalancePercent(List<DailyGraphDto> loggedDays) {
        if (loggedDays == null || loggedDays.isEmpty()) {
            return null;
        }

        List<Double> dailyBalances = new ArrayList<>();

        //Here i calculate balance percentage:
        for (int i = 0; i < loggedDays.size(); i++) {
            DailyGraphDto day = loggedDays.get(i);
            if (day == null) continue;

            if (day.getStandingTime() == null || day.getStandingTime() == -1 || day.getSittingTime() == null || day.getSittingTime() == -1) {
                continue;
            }

            int total = day.getStandingTime() + day.getSittingTime();

            if (total == 0) {
                continue;
            }

            //calculate daily balance percentage:
            double balancePercent = ((double) day.getStandingTime() / (double) total) * 100.0;
            dailyBalances.add(balancePercent);
        }

        if (dailyBalances.isEmpty()) {
            return null;
        }

        //Average balance:
        double sum = 0.0;

        for (int i = 0; i < dailyBalances.size(); i++) {
            sum = sum + dailyBalances.get(i);
        }

        return sum / dailyBalances.size();
    }

    public Double calculateStandingGoalMetPercent(List<DailyGraphDto> allDays) {
        if (allDays == null || allDays.isEmpty()) {
            return null;
        }

        int goalMetDays = 0;
        boolean hasAnyValidStandingData = false;

        for (int i = 0; i < allDays.size(); i++) {
            DailyGraphDto day = allDays.get(i);

            if (day == null) continue;

            Integer standing = day.getStandingTime();

            if (standing == null || standing == -1) {
                continue;
            }

            hasAnyValidStandingData = true;

            //Standing goal: enum>=3)
            if (standing >= 3) {
                goalMetDays++;
            }
        }

        //this means user never logged any standing time at all:
        if (!hasAnyValidStandingData) {
            return null;
        }

        //percentage over 7 days week and not allDays.size();
        return (goalMetDays / 7.0) * 100.0;
    }

    public Double calculateSedentaryLimitMetPercent(List<DailyGraphDto> allDays) {
        if (allDays == null || allDays.isEmpty()) {
            return null;
        }

        int limitMetDays = 0;
        boolean hasAnySedentaryData = false;

        for (int i = 0; i < allDays.size(); i++) {
            DailyGraphDto day = allDays.get(i);

            if (day == null) continue;

            Double fitbitSedentaryHours = day.getFitbitSedentaryHours();

            if (fitbitSedentaryHours == null) {
                continue;
            }

            hasAnySedentaryData = true;

            if (fitbitSedentaryHours < 11.0) {
                limitMetDays++;
            }
        }

        //user never had fitbit sedentary data:
        if (!hasAnySedentaryData) {
            return null;
        }

        //percentage over 7 day week and not allDays.size();
        return (limitMetDays / 7.0) * 100.0;
    }

    public Double calculateBalanceGoalMetPercent(List<DailyGraphDto> allDays) {
        if (allDays == null || allDays.isEmpty()) {
            return null;
        }

        int goalMetDays = 0;
        boolean hasAnyValidActivityData = false;

        for (int i = 0; i < allDays.size(); i++) {
            DailyGraphDto day = allDays.get(i);

            if (day == null) continue;

            Integer standing = day.getStandingTime();
            Integer sitting = day.getSittingTime();

            if (standing == null || standing == -1 || sitting == null || sitting == -1) {
                continue;
            }

            int total = standing + sitting;

            if (total == 0) {
                continue;
            }

            hasAnyValidActivityData = true;

            double standingPercent = ((double) standing / (double) total) * 100.0;

            if (standingPercent >= 60.0) {
                goalMetDays++;
            }
        }

        if (!hasAnyValidActivityData) {
            return null;
        }

        //percentage over 7 days of the week and not allDays.size():
        return (goalMetDays / 7.0) * 100.0;
    }

    public DaySummaryDto getBestStandingDay(List<DailyGraphDto> allDays) {
        if (allDays == null || allDays.isEmpty()) {
            return null;
        }

        DailyGraphDto bestDay = null;

        for (int i = 0; i < allDays.size(); i++) {
            DailyGraphDto day = allDays.get(i);

            if (day == null) continue;

            Integer standing = day.getStandingTime();

            if (standing == null || standing == -1) {
                continue;
            }

            if (bestDay == null || standing > bestDay.getStandingTime()) {
                bestDay = day;
            }
        }

        if (bestDay == null) {
            return null;
        }

        return DaySummaryDto.builder()
                .date(bestDay.getDate())
                .value(bestDay.getStandingTime())
                .build();
    }

    public DaySummaryDto getWorstStandingDay(List<DailyGraphDto> allDays) {
        if (allDays == null || allDays.isEmpty()) {
            return null;
        }

        DailyGraphDto worstDay = null;

        for (int i = 0; i < allDays.size(); i++) {
            DailyGraphDto day = allDays.get(i);

            if (day == null) continue;

            Integer standing = day.getStandingTime();

            if (standing == null || standing == -1) {
                continue;
            }

            if (worstDay == null || standing < worstDay.getStandingTime()) {
                worstDay = day;
            }
        }

        if (worstDay == null) {
            return null;
        }

        return DaySummaryDto.builder()
                .date(worstDay.getDate())
                .value(worstDay.getStandingTime())
                .build();
    }

    public List<ExplanationDto> explainWhyActivityDecreased(
            DaySummaryDto bestStandingDay,
            DaySummaryDto worstStandingDay,
            List<DailyGraphDto> allDays,
            boolean isFitbitConnected
    ) {
        List<ExplanationDto> explanations = new ArrayList<>();

        // Guard clauses (production-safe)
        if (bestStandingDay == null || worstStandingDay == null) {
            return explanations;
        }
        if (allDays == null || allDays.isEmpty()) {
            return explanations;
        }

        String bestDate = bestStandingDay.getDate();
        String worstDate = worstStandingDay.getDate();

        if (bestDate == null || worstDate == null) {
            return explanations;
        }

        DailyGraphDto bestDay = null;
        DailyGraphDto worstDay = null;

        // Find matching days by date (null-safe)
        for (int i = 0; i < allDays.size(); i++) {
            DailyGraphDto day = allDays.get(i);
            if (day == null) continue;

            String date = day.getDate();
            if (date == null) continue;

            if (date.equals(bestDate)) {
                bestDay = day;
            }

            if (date.equals(worstDate)) {
                worstDay = day;
            }

            // Small optimization: break once both found
            if (bestDay != null && worstDay != null) {
                break;
            }
        }

        if (bestDay == null || worstDay == null) {
            return explanations;
        }

        // Pain comparison
        Integer worstPain = worstDay.getPainLevel();
        Integer bestPain = bestDay.getPainLevel();

        if (worstPain != null && worstPain != -1
                && bestPain != null && bestPain != -1
                && worstPain > bestPain) {
            explanations.add(
                    ExplanationDto.builder()
                            .cause("Pain level was higher on the low-activity day")
                            .explanation(
                                    "Higher pain levels can reduce mobility and physical capacity, "
                                            + "making it more difficult to remain active throughout the day."
                            )
                            .build()
            );
        }

        // Stress level comparison
        Integer worstStress = worstDay.getStressLevel();
        Integer bestStress = bestDay.getStressLevel();

        if (worstStress != null && worstStress != -1
                && bestStress != null && bestStress != -1
                && worstStress > bestStress) {
            explanations.add(
                    ExplanationDto.builder()
                            .cause("Stress levels were higher on the low-activity day")
                            .explanation(
                                    "Elevated stress can reduce energy levels and motivation, "
                                            + "making it harder to maintain regular activity patterns."
                            )
                            .build()
            );
        }

        // Sleep comparison
        Double worstSleepHours = getSleepHours(worstDay, isFitbitConnected);
        Double bestSleepHours = getSleepHours(bestDay, isFitbitConnected);

        if (worstSleepHours != null && bestSleepHours != null
                && worstSleepHours < bestSleepHours - 0.5) {
            explanations.add(
                    ExplanationDto.builder()
                            .cause(
                                    String.format(
                                            "Sleep duration was %.1f hours (vs %.1f hours on the high-activity day)",
                                            worstSleepHours,
                                            bestSleepHours
                                    )
                            )
                            .explanation(
                                    "Insufficient sleep reduces physical recovery and energy availability, "
                                            + "which can negatively affect daily activity levels."
                            )
                            .build()
            );
        }

        return explanations;
    }

    private Double getSleepHours(DailyGraphDto day, boolean isFitbitConnected) {

        if (isFitbitConnected) {
            Integer minutes = day.getFitbitTotalMinutesAsleep();
            if (minutes != null) {
                return minutes / 60.0;
            }
        }

        Integer sleepEnum = day.getSleepDuration();
        if (sleepEnum == null || sleepEnum == -1) {
            return null;
        }

        //Manual sleep enum -> hours
        return switch (sleepEnum) {
            case 0 -> 4.5;
            case 1 -> 5.5;
            case 2 -> 6.5;
            case 3 -> 7.5;
            case 4 -> 8.5;
            default -> null;
        };
    }

    public Integer calculateFlareUpDays(List<DailyGraphDto> allDays) {
        if (allDays == null || allDays.isEmpty()) {
            return 0;
        }

        int countOfFlareUpDays = 0;

        for (int i = 0; i < allDays.size(); i++) {
            DailyGraphDto currentDay = allDays.get(i);
            if (currentDay != null && Boolean.TRUE.equals(currentDay.getFlareUp())) {
                countOfFlareUpDays++;
            }
        }

        return countOfFlareUpDays;
    }

    public Integer calculateNonFlareUpDays(List<DailyGraphDto> allDays) {
        if (allDays == null || allDays.isEmpty()) {
            return 0;
        }

        int countOfNonFlareUpDays = 0;

        for (int i = 0; i < allDays.size(); i++) {
            DailyGraphDto currentDay = allDays.get(i);
            if (currentDay != null && !Boolean.TRUE.equals(currentDay.getFlareUp())) {
                countOfNonFlareUpDays++;
            }
        }

        return countOfNonFlareUpDays;
    }

    public Double calculateFlareAvgStanding(List<DailyGraphDto> allDays, boolean isFlareUp) {
        if (allDays == null || allDays.isEmpty()) {
            return null;
        }

        double sum = 0.0;
        int count = 0;

        for (int i = 0; i < allDays.size(); i++) {
            DailyGraphDto day = allDays.get(i);

            if (day == null) continue;
            boolean isDayFlareUp = Boolean.TRUE.equals(day.getFlareUp());

            if (isFlareUp == isDayFlareUp) {
                Integer standing = day.getStandingTime();
                if (standing != null && standing != -1) {
                    sum = sum + standing;
                    count++;
                }
            }
        }

        return count == 0 ? null : sum / count;
    }

    public Double calculateFlareAvgSitting(List<DailyGraphDto> allDays, boolean isFlareUp) {
        if (allDays == null || allDays.isEmpty()) {
            return null;
        }

        double sum = 0.0;
        int count = 0;

        for (int i = 0; i < allDays.size(); i++) {
            DailyGraphDto day = allDays.get(i);

            if (day == null) continue;
            boolean isDayFlareUp = Boolean.TRUE.equals(day.getFlareUp());

            if (isFlareUp == isDayFlareUp) {
                Integer sitting = day.getSittingTime();
                if (sitting != null && sitting != -1) {
                    sum = sum + sitting;
                    count++;
                }
            }
        }

        return count == 0 ? null : sum / count;
    }

    public Double calculateFlareAvgSedentaryHours(List<DailyGraphDto> allDays, boolean isFitbitConnected, boolean isFlareUp) {
        if (!isFitbitConnected || allDays == null || allDays.isEmpty()) {
            return null;
        }

        double sum = 0.0;
        int count = 0;

        for (int i = 0; i < allDays.size(); i++) {
            DailyGraphDto day = allDays.get(i);

            if (day == null) continue;

            boolean isDayFlareUp = Boolean.TRUE.equals(day.getFlareUp());

            if (isFlareUp == isDayFlareUp) {
                Double sedentary = day.getFitbitSedentaryHours();
                if (sedentary != null) {
                    sum = sum + sedentary;
                    count++;
                }
            }
        }

        return count == 0 ? null : sum / count;
    }

    public Double calculateDeltaPercent(Double flareAvg, Double nonFlareAvg) {
        if (flareAvg == null || nonFlareAvg == null) {
            return null;
        }

        if (nonFlareAvg <= 0.0) {
            return null;
        }

        return ((flareAvg - nonFlareAvg) / nonFlareAvg) * 100.0;
    }

    public String buildFlareUpActivitySummaryText(Double standingDeltaPercent, Double sedentaryDeltaPercent, boolean isFitbitConnected) {
        if (standingDeltaPercent != null) {
            double abs = Math.abs(standingDeltaPercent);
            if (abs >= 10.0) {
                return standingDeltaPercent < 0
                        ? String.format("On flare-up days, standing drops by ~%.0f%%.", abs)
                        : String.format("On flare-up days, standing increases by ~%.0f%%.", abs);
            }
        }

        if (isFitbitConnected && sedentaryDeltaPercent != null) {
            double abs = Math.abs(sedentaryDeltaPercent);
            if (abs >= 10.0) {
                return sedentaryDeltaPercent > 0
                        ? String.format("On flare-up days, sedentary time rises by ~%.0f%%.", abs)
                        : String.format("On flare-up days, sedentary time drops by ~%.0f%%.", abs);
            }
        }

        return "Not enough data this week to quantify flare-up impact.";
    }

}
