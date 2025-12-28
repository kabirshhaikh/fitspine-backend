package com.fitspine.helper;

import com.fitspine.dto.DailyGraphDto;
import com.fitspine.dto.DaySummaryDto;
import com.fitspine.dto.ExplanationDto;
import com.fitspine.dto.TrendResultDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class HeartCalculatorHelper {
    public List<DailyGraphDto> getValidHeartRateDays(List<DailyGraphDto> allDays, boolean isFitbitConnected) {
        List<DailyGraphDto> list = new ArrayList<>();

        if (allDays == null || allDays.isEmpty()) {
            return list;
        }

        for (int i = 0; i < allDays.size(); i++) {
            DailyGraphDto day = allDays.get(i);
            Integer restingHeartRate = this.getHeartRateValue(day, isFitbitConnected);
            if (restingHeartRate != null) {
                list.add(day);
            }
        }

        return list;
    }

    public Integer getHeartRateValue(DailyGraphDto day, boolean isFitbitConnected) {
        if (isFitbitConnected) {
            if (day.getFitbitRestingHeartRate() != null) {
                return day.getFitbitRestingHeartRate();
            } else if (day.getManualRestingHeartRate() != null) {
                return day.getManualRestingHeartRate();
            } else {
                return null;
            }
        } else {
            return day.getManualRestingHeartRate();
        }
    }

    public List<Integer> getHeartRateValues(List<DailyGraphDto> validHeartRateDays, boolean isFitbitConnected) {
        List<Integer> list = new ArrayList<>();

        for (int i = 0; i < validHeartRateDays.size(); i++) {
            DailyGraphDto day = validHeartRateDays.get(i);
            Integer restingHeartRate = this.getHeartRateValue(day, isFitbitConnected);

            if (restingHeartRate != null) {
                list.add(restingHeartRate);
            }
        }

        return list;
    }

    public Double calculateAverageRestingHeartRate(List<Integer> heartRateValues) {
        if (heartRateValues == null || heartRateValues.isEmpty()) {
            return null;
        }

        int sum = 0;
        int count = 0;

        for (int i = 0; i < heartRateValues.size(); i++) {
            if (heartRateValues.get(i) != null && heartRateValues.get(i) != -1) {
                sum = sum + heartRateValues.get(i);
                count++;
            }
        }

        return count == 0 ? null : (double) sum / count;
    }

    public TrendResultDto calculateTrend(List<Integer> heartRateValues, String metricName) {
        List<Integer> validValues = new ArrayList<>();
        for (int i = 0; i < heartRateValues.size(); i++) {
            if (heartRateValues.get(i) != null && heartRateValues.get(i) != -1) {
                validValues.add(heartRateValues.get(i));
            }
        }

        if (validValues.size() < 2) {
            return null;
        }

        int mid = validValues.size() / 2;

        List<Integer> firstHalf = validValues.subList(0, mid);
        List<Integer> secondHalf = validValues.subList(mid, validValues.size());

        double firstAvg = firstHalf.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);

        double secondAvg = secondHalf.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0);

        double diff = secondAvg - firstAvg;

        double percentageChange = firstAvg != 0 ? Math.abs((diff / firstAvg) * 100) : 0;

        if (percentageChange < 5) {
            return TrendResultDto.builder().direction("stable").changePercentage(0.0).build();
        }

        boolean lowerIsBetter = metricName.equals("restingHeartRate");

        String direction;
        if (lowerIsBetter) {
            direction = diff < 0 ? "better" : "worse";
        } else {
            direction = diff > 0 ? "better" : "worse";
        }

        return TrendResultDto.builder()
                .direction(direction)
                .changePercentage(percentageChange)
                .build();
    }

    public Double calculateRecoveryConsistencyPercent(List<Integer> heartRateValues, Double averageRestingHeartRate) {
        if (heartRateValues == null || heartRateValues.size() < 2 || averageRestingHeartRate == null || averageRestingHeartRate == 0) {
            return null;
        }

        double varianceSum = 0.0;
        int count = 0;

        for (int i = 0; i < heartRateValues.size(); i++) {
            if (heartRateValues.get(i) != null) {
                varianceSum = varianceSum + Math.pow(heartRateValues.get(i) - averageRestingHeartRate, 2);
                count++;
            }
        }

        if (count < 2) {
            return null;
        }

        double variance = varianceSum / count;
        double stdDev = Math.sqrt(variance);

        return (1 - (stdDev / averageRestingHeartRate)) * 100;
    }

    public List<String> detectStressHeartRateCorrelation(List<DailyGraphDto> validHeartRateDays, boolean isFitbitConnected) {
        List<String> insights = new ArrayList<>();

        //build stress <-> hr pairs:
        List<DailyGraphDto> stressHrDays = new ArrayList<>();

        for (int i = 0; i < validHeartRateDays.size(); i++) {
            DailyGraphDto day = validHeartRateDays.get(i);
            Integer stress = day.getStressLevel();
            Integer hr = this.getHeartRateValue(day, isFitbitConnected);

            if (stress != null && hr != null) {
                stressHrDays.add(day);
            }
        }

        if (stressHrDays.size() < 2) {
            return insights;
        }


        //sort by stress level
        stressHrDays.sort((d1, d2) -> d1.getStressLevel().compareTo(d2.getStressLevel()));

        int mid = stressHrDays.size() / 2;

        List<DailyGraphDto> lowStress = stressHrDays.subList(0, mid);
        List<DailyGraphDto> highStress = stressHrDays.subList(mid, stressHrDays.size());

        Double lowStressAvgHr = this.calculateAverageRestingHeartRate(
                getHeartRateValues(lowStress, isFitbitConnected)
        );

        Double highStressAvgHr = this.calculateAverageRestingHeartRate(
                getHeartRateValues(highStress, isFitbitConnected)
        );

        if (lowStressAvgHr == null || highStressAvgHr == null) {
            return insights;
        }

        double diff = highStressAvgHr - lowStressAvgHr;

        //threshold: 2 bpm
        if (Math.abs(diff) > 2) {
            if (diff > 0) {
                insights.add("Heart rate was " + Math.round(diff)
                        + " bpm higher on high-stress days");
            } else {
                insights.add("Lower stress days had average HR "
                        + Math.round(Math.abs(diff)) + " bpm lower");
            }
        }

        return insights;
    }

    public DaySummaryDto getBestHeartRateDay(List<DailyGraphDto> validHeartRateDays, boolean isFitbitConnected) {
        DailyGraphDto bestDay = null;
        Integer bestHr = null;

        for (int i = 0; i < validHeartRateDays.size(); i++) {
            DailyGraphDto day = validHeartRateDays.get(i);
            Integer hr = this.getHeartRateValue(day, isFitbitConnected);
            if (hr == null) {
                continue;
            }

            if (bestHr == null || hr < bestHr) {
                bestHr = hr;
                bestDay = day;
            }
        }

        if (bestDay == null) {
            return null;
        }

        return DaySummaryDto.builder()
                .value(bestHr)
                .date(bestDay.getDate())
                .build();
    }

    public DaySummaryDto getWorstHeartRateDay(List<DailyGraphDto> validHeartRateDays, boolean isFitbitConnected) {
        DailyGraphDto worstDay = null;
        Integer worstHr = null;

        for (int i = 0; i < validHeartRateDays.size(); i++) {
            DailyGraphDto day = validHeartRateDays.get(i);
            Integer hr = this.getHeartRateValue(day, isFitbitConnected);
            if (hr == null) {
                continue;
            }

            if (worstHr == null || hr > worstHr) {
                worstHr = hr;
                worstDay = day;
            }
        }

        if (worstDay == null) {
            return null;
        }

        return DaySummaryDto.builder()
                .value(worstHr)
                .date(worstDay.getDate())
                .build();
    }

    public List<ExplanationDto> explainWhyHeartRateChanged(DailyGraphDto bestDay, DailyGraphDto worstDay, boolean isFitbitConnected) {
        List<ExplanationDto> explanations = new ArrayList<>();

        if (worstDay == null || bestDay == null) {
            return explanations;
        }

        //Flare up explanation:
        Boolean worstFlare = worstDay.getFlareUp();
        Boolean bestFlare = bestDay.getFlareUp();

        if (Boolean.TRUE.equals(worstFlare) && !Boolean.TRUE.equals(bestFlare)) {
            explanations.add(ExplanationDto.builder()
                    .cause("A flare-up was reported on the worst heart rate day")
                    .explanation("Flare-ups can temporarily increase nervous system activation, "
                            + "which may raise resting heart rate.")
                    .build());
        }

        //Stress comparison:
        Integer worstStress = worstDay.getStressLevel();
        Integer bestStress = bestDay.getStressLevel();

        if (worstStress != null && bestStress != null && worstStress > bestStress) {
            explanations.add(ExplanationDto.builder()
                    .cause("Stress level was higher on the worst heart rate day")
                    .explanation("Higher stress activates the sympathetic nervous system, "
                            + "which can increase resting heart rate.")
                    .build());
        }

        //Sleep comparison:
        Double worstSleepHours = this.getSleepHours(worstDay, isFitbitConnected);
        Double bestSleepHours = this.getSleepHours(bestDay, isFitbitConnected);

        if (worstSleepHours != null && bestSleepHours != null &&
                worstSleepHours < bestSleepHours - 0.5) {
            explanations.add(ExplanationDto.builder()
                    .cause("Sleep duration was lower on the worst heart rate day")
                    .explanation("Insufficient sleep reduces recovery and increases physiological stress, "
                            + "which may elevate resting heart rate.")
                    .build());
        }

        //Pain comparison
        Integer worstPain = worstDay.getPainLevel();
        Integer bestPain = bestDay.getPainLevel();

        if (worstPain != null && bestPain != null && worstPain > bestPain) {
            explanations.add(ExplanationDto.builder()
                    .cause("Pain level was higher on the worst heart rate day")
                    .explanation("Increased pain can trigger stress responses in the body, "
                            + "leading to elevated resting heart rate.")
                    .build());
        }

        return explanations;
    }

    public Double getSleepHours(DailyGraphDto day, boolean isFitbitConnected) {
        if (isFitbitConnected) {
            if (day.getFitbitTotalMinutesAsleep() != null) {
                return day.getFitbitTotalMinutesAsleep() / 60.0;
            }
        }

        Integer sleepEnum = day.getSleepDuration();
        if (sleepEnum == null || sleepEnum == -1) {
            return null;
        }

        switch (sleepEnum) {
            case 0:
                return 4.5;
            case 1:
                return 5.5;
            case 2:
                return 6.5;
            case 3:
                return 7.5;
            case 4:
                return 8.5;
            default:
                return null;
        }
    }


}
