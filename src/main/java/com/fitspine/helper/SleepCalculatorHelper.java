package com.fitspine.helper;

import com.fitspine.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class SleepCalculatorHelper {
    public List<DailyGraphDto> getValidSleepDays(List<DailyGraphDto> allDays, boolean isFitbitConnected) {
        List<DailyGraphDto> list = new ArrayList<>();

        if (allDays == null || allDays.isEmpty()) {
            return list;
        }

        for (int i = 0; i < allDays.size(); i++) {
            DailyGraphDto day = allDays.get(i);
            if (day == null) {
                continue;
            }

            //If fitbit data is present then add that day and continue, no need to fallback to manual:
            if (isFitbitConnected && day.getFitbitTotalMinutesAsleep() != null) {
                list.add(day);
                continue;
            }

            //If fitbit data is not available then process manual data:
            Integer sleepDuration = day.getSleepDuration();
            if (sleepDuration != null && sleepDuration != -1) {
                list.add(day);
            }
        }

        return list;
    }

    public List<Double> getSleepHoursValues(List<DailyGraphDto> validSleepDays, boolean isFitbitConnected) {
        List<Double> list = new ArrayList<>();

        if (validSleepDays == null || validSleepDays.isEmpty()) {
            return list;
        }

        for (int i = 0; i < validSleepDays.size(); i++) {
            DailyGraphDto day = validSleepDays.get(i);

            if (day == null) {
                continue;
            }

            Double sleepHours = getSleepHours(day, isFitbitConnected);
            if (sleepHours != null) {
                list.add(sleepHours);
            }
        }

        return list;
    }

    public Double getSleepHours(DailyGraphDto day, boolean isFitbitConnected) {
        if (day == null) {
            return null;
        }

        //if fitbit is connected then grab that that:
        if (isFitbitConnected) {
            Integer minutesAsleep = day.getFitbitTotalMinutesAsleep();
            if (minutesAsleep != null) {
                return minutesAsleep / 60.0;
            }
        }

        //if fitbit is not connected or fitbit is connected but no data from fitibit is available, then in that case fallback to manual:
        Integer sleepDurationEnum = day.getSleepDuration();
        if (sleepDurationEnum == null || sleepDurationEnum == -1) {
            return null;
        }

        return switch (sleepDurationEnum) {
            case 0 -> 4.5; //<5h
            case 1 -> 5.5; //5–6h
            case 2 -> 6.5; //6–7h
            case 3 -> 7.5; //7–8h
            case 4 -> 8.5; //>8h
            default -> null;
        };
    }

    public Double calculateAverageSleepHours(List<Double> sleepHoursValues) {
        if (sleepHoursValues == null || sleepHoursValues.isEmpty()) {
            return null;
        }

        double sum = 0.0;

        for (int i = 0; i < sleepHoursValues.size(); i++) {
            sum = sum + sleepHoursValues.get(i);
        }

        return sum / sleepHoursValues.size();
    }

    public TrendResultDto calculateSleepTrend(List<Double> sleepHoursValues) {
        if (sleepHoursValues == null || sleepHoursValues.isEmpty()) {
            return null;
        }

        List<Double> validValues = new ArrayList<>();
        for (int i = 0; i < sleepHoursValues.size(); i++) {
            if (sleepHoursValues.get(i) != null) {
                validValues.add(sleepHoursValues.get(i));
            }
        }

        if (validValues.size() < 2) {
            return null;
        }

        int mid = validValues.size() / 2;

        List<Double> firstHalf = validValues.subList(0, mid);
        List<Double> secondHalf = validValues.subList(mid, validValues.size());

        //calculate averages:
        double firstAvg = firstHalf.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);

        double secondAvg = secondHalf.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0);


        //diff:
        double diff = secondAvg - firstAvg;

        //percentage change:
        double percentageChange = firstAvg != 0 ? Math.abs((diff / firstAvg) * 100) : 0;

        //threshold:
        if (percentageChange < 5) {
            return TrendResultDto.builder()
                    .direction("stable")
                    .changePercentage(0.0)
                    .build();
        }

        //direction, higher sleep is better:
        String direction = diff > 0 ? "better" : "worse";

        return TrendResultDto.builder()
                .direction(direction)
                .changePercentage(percentageChange)
                .build();
    }

    public SleepQualityDto getSleepQualityLabel(Double hours) {
        if (hours == null) {
            return null;
        }

        if (hours >= 7 && hours <= 9) {
            return SleepQualityDto.builder()
                    .label("Optimal")
                    .color("#4caf50")
                    .build();
        } else if (hours >= 6) {
            return SleepQualityDto.builder()
                    .label("Good")
                    .color("#8bc34a")
                    .build();
        } else if (hours >= 5) {
            return SleepQualityDto.builder()
                    .label("Fair")
                    .color("#ff9800")
                    .build();
        } else if (hours < 5) {
            return SleepQualityDto.builder()
                    .label("Insufficient")
                    .color("#f44336")
                    .build();
        } else {
            return SleepQualityDto.builder()
                    .label("Excessive")
                    .color("#ff9800")
                    .build();
        }
    }

    public Double calculateSleepConsistency(List<Double> sleepHoursValues, Double averageSleepHours) {
        if (sleepHoursValues == null || sleepHoursValues.size() < 2 || averageSleepHours == null || averageSleepHours == 0) {
            return null;
        }

        double varianceSum = 0.0;
        int count = 0;

        for (int i = 0; i < sleepHoursValues.size(); i++) {
            if (sleepHoursValues.get(i) != null) {
                varianceSum = varianceSum + Math.pow(sleepHoursValues.get(i) - averageSleepHours, 2);
                count++;
            }
        }

        if (count < 2) {
            return null;
        }

        double variance = varianceSum / count;
        double stdDev = Math.sqrt(variance);

        return (1 - (stdDev / averageSleepHours)) * 100;
    }

    public DaySummaryDto getBestDay(List<DailyGraphDto> allDays, boolean isFitbitConnected) {
        DailyGraphDto bestDay = null;
        Double bestSleep = null;

        for (int i = 0; i < allDays.size(); i++) {
            DailyGraphDto day = allDays.get(i);
            if (day == null) {
                continue;
            }

            Double sleepHours = getSleepHours(day, isFitbitConnected);
            if (sleepHours == null) {
                continue;
            }

            if (bestSleep == null || sleepHours > bestSleep) {
                bestSleep = sleepHours;
                bestDay = day;
            }
        }

        if (bestDay == null) {
            return null;
        }


        return DaySummaryDto.builder()
                .date(bestDay.getDate())
                .value((int) Math.round(bestSleep))
                .build();
    }

    public DaySummaryDto getWorstSleepDay(List<DailyGraphDto> allDays, boolean isFitbitConnected) {
        DailyGraphDto worstDay = null;
        Double worstSleep = null;

        for (int i = 0; i < allDays.size(); i++) {
            DailyGraphDto day = allDays.get(i);
            if (day == null) {
                continue;
            }

            Double sleepHours = getSleepHours(day, isFitbitConnected);
            if (sleepHours == null) {
                continue;
            }

            if (worstSleep == null || sleepHours < worstSleep) {
                worstSleep = sleepHours;
                worstDay = day;
            }
        }

        if (worstDay == null) {
            return null;
        }


        return DaySummaryDto.builder()
                .date(worstDay.getDate())
                .value((int) Math.round(worstSleep))
                .build();
    }

    public DailyGraphDto findDayByDate(List<DailyGraphDto> allDays, String date) {
        if (allDays == null || date == null) {
            return null;
        }

        for (int i = 0; i < allDays.size(); i++) {
            DailyGraphDto day = allDays.get(i);
            if (day == null) {
                continue;
            }

            if (date.equals(day.getDate())) {
                return day;
            }
        }

        return null;
    }

    public List<ExplanationDto> explainWhySleepChanged(DailyGraphDto bestDay, DailyGraphDto worstDay) {
        List<ExplanationDto> explanations = new ArrayList<>();

        if (bestDay == null || worstDay == null) {
            return explanations;
        }

        //Flare up explanation:
        Boolean worstFlare = worstDay.getFlareUp();
        Boolean bestFlare = bestDay.getFlareUp();

        if (Boolean.TRUE.equals(worstFlare) && !Boolean.TRUE.equals(bestFlare)) {
            explanations.add(ExplanationDto.builder()
                    .cause("A flare-up was reported on the worst sleep day")
                    .explanation(
                            "Flare-ups can increase nervous system arousal and physical discomfort, "
                                    + "making it harder to fall asleep and maintain restful sleep."
                    )
                    .build());
        }

        //Stress comparison
        Integer bestStress = bestDay.getStressLevel();
        Integer worstStress = worstDay.getStressLevel();

        if (bestStress != null && worstStress != null && worstStress > bestStress) {
            explanations.add(ExplanationDto.builder()
                    .cause("Stress level was higher on the worst sleep day")
                    .explanation("Higher stress can activate the nervous system and interfere with sleep quality and duration.")
                    .build());
        }

        //Pain comparison
        Integer bestPain = bestDay.getPainLevel();
        Integer worstPain = worstDay.getPainLevel();

        if (bestPain != null && worstPain != null && worstPain > bestPain) {
            explanations.add(ExplanationDto.builder()
                    .cause("Pain level was higher on the worst sleep day")
                    .explanation("Increased pain can disrupt sleep cycles and reduce overall sleep duration.")
                    .build());
        }

        //Sedentary time comparison (hours)
        Double bestSedentary = bestDay.getFitbitSedentaryHours();
        Double worstSedentary = worstDay.getFitbitSedentaryHours();

        if (bestSedentary != null && worstSedentary != null &&
                worstSedentary > bestSedentary + 1) {
            explanations.add(ExplanationDto.builder()
                    .cause("Sedentary time was higher on the worst sleep day")
                    .explanation("Longer sedentary periods may reduce physical fatigue and delay sleep onset.")
                    .build());
        }

        return explanations;
    }


}
