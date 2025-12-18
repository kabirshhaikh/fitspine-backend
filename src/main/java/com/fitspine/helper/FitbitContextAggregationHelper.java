package com.fitspine.helper;

import com.fitspine.dto.*;
import com.fitspine.model.*;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.time.LocalDate;
import java.util.*;

@Component
public class FitbitContextAggregationHelper {
    //Helper functions to return list of fitbit normalized metrics:
    public Map<LocalDate, Integer> getPainLevels(List<ManualDailyLog> manualDailyLogs) {
        Map<LocalDate, Integer> painLevels = new HashMap<>();

        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return painLevels;
        }

        for (int i = 0; i < manualDailyLogs.size(); i++) {
            ManualDailyLog log = manualDailyLogs.get(i);


            if (log != null && log.getPainLevel() != null) {
                LocalDate logDate = log.getLogDate();
                painLevels.put(logDate, EnumScoreHelper.pain(log.getPainLevel()));
            }
        }

        return painLevels;
    }

    public Map<LocalDate, Integer> getSleepDuration(List<ManualDailyLog> manualDailyLogs) {
        Map<LocalDate, Integer> sleepDuration = new HashMap<>();

        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return sleepDuration;
        }

        for (int i = 0; i < manualDailyLogs.size(); i++) {
            ManualDailyLog log = manualDailyLogs.get(i);

            if (log != null && log.getSleepDuration() != null) {
                LocalDate logDate = log.getLogDate();
                sleepDuration.put(logDate, EnumScoreHelper.sleepDuration(log.getSleepDuration()));
            }

        }

        return sleepDuration;
    }

    public Map<LocalDate, Integer> getNightWakeUps(List<ManualDailyLog> manualDailyLogs) {
        Map<LocalDate, Integer> nightWakeUps = new HashMap<>();

        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return nightWakeUps;
        }

        for (int i = 0; i < manualDailyLogs.size(); i++) {
            ManualDailyLog log = manualDailyLogs.get(i);

            if (log != null && log.getNightWakeUps() != null) {
                LocalDate logDate = log.getLogDate();
                nightWakeUps.put(logDate, EnumScoreHelper.nightWakeUps(log.getNightWakeUps()));
            }
        }

        return nightWakeUps;
    }

    public Map<LocalDate, Integer> getManualRestingHeartRate(List<ManualDailyLog> manualDailyLogs) {
        Map<LocalDate, Integer> manualRestingHeartRate = new HashMap<>();

        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return manualRestingHeartRate;
        }

        for (int i = 0; i < manualDailyLogs.size(); i++) {
            ManualDailyLog log = manualDailyLogs.get(i);

            if (log != null && log.getRestingHeartRate() != null) {
                LocalDate logDate = log.getLogDate();
                manualRestingHeartRate.put(logDate, log.getRestingHeartRate());
            }
        }

        return manualRestingHeartRate;
    }

    public Map<LocalDate, Integer> getMorningStiffness(List<ManualDailyLog> manualDailyLogs) {
        Map<LocalDate, Integer> morningStiffness = new HashMap<>();

        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return morningStiffness;
        }

        for (int i = 0; i < manualDailyLogs.size(); i++) {
            ManualDailyLog log = manualDailyLogs.get(i);
            if (log != null && log.getMorningStiffness() != null) {
                LocalDate logDate = log.getLogDate();
                morningStiffness.put(logDate, EnumScoreHelper.morningStiffness(log.getMorningStiffness()));
            }
        }

        return morningStiffness;
    }

    public Map<LocalDate, Integer> getSittingTime(List<ManualDailyLog> manualDailyLogs) {
        Map<LocalDate, Integer> sittingTime = new HashMap<>();

        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return sittingTime;
        }

        for (int i = 0; i < manualDailyLogs.size(); i++) {
            ManualDailyLog log = manualDailyLogs.get(i);
            if (log != null && log.getSittingTime() != null) {
                LocalDate logDate = log.getLogDate();
                sittingTime.put(logDate, EnumScoreHelper.sittingTime(log.getSittingTime()));
            }
        }

        return sittingTime;
    }

    public Map<LocalDate, Integer> getStandingTime(List<ManualDailyLog> manualDailyLogs) {
        Map<LocalDate, Integer> standingTime = new HashMap<>();

        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return standingTime;
        }

        for (int i = 0; i < manualDailyLogs.size(); i++) {
            ManualDailyLog log = manualDailyLogs.get(i);
            if (log != null && log.getStandingTime() != null) {
                LocalDate logDate = log.getLogDate();
                standingTime.put(logDate, EnumScoreHelper.standingTime(log.getStandingTime()));
            }
        }

        return standingTime;
    }

    public Map<LocalDate, Integer> getStressLevel(List<ManualDailyLog> manualDailyLogs) {
        Map<LocalDate, Integer> stressLevel = new HashMap<>();

        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return stressLevel;
        }

        for (int i = 0; i < manualDailyLogs.size(); i++) {
            ManualDailyLog log = manualDailyLogs.get(i);
            if (log != null && log.getStressLevel() != null) {
                LocalDate logDate = log.getLogDate();
                stressLevel.put(logDate, EnumScoreHelper.stressLevel(log.getStressLevel()));
            }
        }

        return stressLevel;
    }

    public Map<LocalDate, Double> getSedentaryHours(List<FitbitActivitySummariesLog> activitySummariesLogs) {
        Map<LocalDate, Double> sedentaryHours = new HashMap<>();

        if (activitySummariesLogs == null || activitySummariesLogs.isEmpty()) {
            return sedentaryHours;
        }

        for (int i = 0; i < activitySummariesLogs.size(); i++) {
            FitbitActivitySummariesLog log = activitySummariesLogs.get(i);

            if (log != null && log.getSedentaryMinutes() != null) {
                LocalDate logDate = log.getLogDate();
                int sedentaryMinutes = log.getSedentaryMinutes();
                double hours = Math.round((sedentaryMinutes / 60.0) * 10.0) / 10.0;
                sedentaryHours.put(logDate, hours);
            }
        }

        return sedentaryHours;
    }

    public List<DailyGraphDto> getDailyDataBetweenDates(LocalDate startDate,
                                                        LocalDate endDate,
                                                        Map<LocalDate, Integer> restingHeartRate,
                                                        Map<LocalDate, Integer> painLevels,
                                                        Map<LocalDate, Integer> morningStiffness,
                                                        Map<LocalDate, Integer> sittingTime,
                                                        Map<LocalDate, Integer> standingTime,
                                                        Map<LocalDate, Integer> stressLevel,
                                                        Map<LocalDate, Double> sedentaryHours,
                                                        Map<LocalDate, Integer> sleepDurations,
                                                        Map<LocalDate, Integer> nightWakeUps,
                                                        Map<LocalDate, Integer> manualRestingHeartRate
    ) {
        List<DailyGraphDto> dailyData = new ArrayList<>();

        //Basically int i=0; i<=endDate; date++
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {

            dailyData.add(
                    DailyGraphDto.builder()
                            .date(date.toString())
                            .painLevel(painLevels.getOrDefault(date, null))
                            .morningStiffness(morningStiffness.getOrDefault(date, null))
                            .sittingTime(sittingTime.getOrDefault(date, null))
                            .standingTime(standingTime.getOrDefault(date, null))
                            .stressLevel(stressLevel.getOrDefault(date, null))
                            .fitbitRestingHeartRate(restingHeartRate.getOrDefault(date, null))
                            .sedentaryHours(sedentaryHours.getOrDefault(date, null))
                            .sleepDuration(sleepDurations.getOrDefault(date, null))
                            .nightWakeUps(nightWakeUps.getOrDefault(date, null))
                            .manualRestingHeartRate(manualRestingHeartRate.getOrDefault(date, null))
                            .build()
            );
        }

        return dailyData;
    }

    public Map<LocalDate, Integer> getRestingHeartRateForWeeklyGraph(List<FitbitActivitiesHeartLog> heartLogs) {
        Map<LocalDate, Integer> restingHeartRates = new HashMap<>();

        if (heartLogs == null || heartLogs.isEmpty()) {
            return restingHeartRates;
        }

        for (int i = 0; i < heartLogs.size(); i++) {
            FitbitActivitiesHeartLog heartLog = heartLogs.get(i);
            if (heartLog == null || heartLog.getValues() == null || heartLog.getLogDate() == null) continue;

            LocalDate logDate = heartLog.getLogDate();
            List<FitbitActivitiesHeartValueLog> valueLogs = heartLog.getValues();
            if (valueLogs == null || valueLogs.isEmpty()) continue;

            for (int j = 0; j < valueLogs.size(); j++) {
                FitbitActivitiesHeartValueLog heartValueLog = valueLogs.get(j);
                if (heartValueLog != null && heartValueLog.getRestingHeartRate() != null) {
                    restingHeartRates.put(logDate, valueLogs.get(j).getRestingHeartRate());
                }
            }
        }

        return restingHeartRates;
    }

    public List<Integer> getRestingHeartRate(List<FitbitActivitiesHeartLog> heartLogs) {
        List<Integer> restingHeartRates = new ArrayList<>();

        if (heartLogs == null || heartLogs.isEmpty()) {
            return restingHeartRates;
        }

        for (int i = 0; i < heartLogs.size(); i++) {
            FitbitActivitiesHeartLog heartLog = heartLogs.get(i);
            if (heartLog == null || heartLog.getValues() == null) continue;

            List<FitbitActivitiesHeartValueLog> valueLogs = heartLog.getValues();
            if (valueLogs == null || valueLogs.isEmpty()) continue;

            for (int j = 0; j < valueLogs.size(); j++) {
                FitbitActivitiesHeartValueLog heartValueLog = valueLogs.get(j);
                if (heartValueLog == null) continue;
                Integer rhr = heartValueLog.getRestingHeartRate();
                if (rhr != null) {
                    restingHeartRates.add(rhr);
                }
            }
        }

        return restingHeartRates;
    }

    public List<FitbitActivitySummariesMetricDto> getActivityMetric(List<FitbitActivitySummariesLog> activitySummariesLogs) {
        List<FitbitActivitySummariesMetricDto> metric = new ArrayList<>();
        if (activitySummariesLogs == null || activitySummariesLogs.isEmpty()) {
            return metric;
        }

        for (int i = 0; i < activitySummariesLogs.size(); i++) {
            FitbitActivitySummariesLog log = activitySummariesLogs.get(i);
            if (log == null) continue;
            metric.add(FitbitActivitySummariesMetricDto.builder()
                    .caloriesOut(log.getCaloriesOut())
                    .steps(log.getSteps())
                    .sedentaryMinutes(log.getSedentaryMinutes())
                    .build()
            );
        }

        return metric;
    }

    public List<FitbitActivityGoalsLogMetricDto> getGoalsMetrics(List<FitbitActivityGoalsLog> activityGoalsLogs) {
        List<FitbitActivityGoalsLogMetricDto> activityGoalsLogMetric = new ArrayList<>();
        if (activityGoalsLogs == null || activityGoalsLogs.isEmpty()) {
            return activityGoalsLogMetric;
        }

        for (int i = 0; i < activityGoalsLogs.size(); i++) {
            FitbitActivityGoalsLog log = activityGoalsLogs.get(i);
            if (log == null || log.getActiveMinutes() == null) continue;
            activityGoalsLogMetric.add(FitbitActivityGoalsLogMetricDto.builder()
                    .activeMinutes(log.getActiveMinutes())
                    .build()
            );
        }

        return activityGoalsLogMetric;
    }

    public List<FitbitSleepLogMetricDto> getSleepLogMetric(List<FitbitSleepLog> sleepLogs) {
        List<FitbitSleepLogMetricDto> sleepLogMetrics = new ArrayList<>();
        if (sleepLogs == null || sleepLogs.isEmpty()) {
            return sleepLogMetrics;
        }

        for (int i = 0; i < sleepLogs.size(); i++) {
            FitbitSleepLog log = sleepLogs.get(i);
            if (log == null || log.getEfficiency() == null) continue;
            sleepLogMetrics.add(FitbitSleepLogMetricDto.builder()
                    .efficiency(log.getEfficiency())
                    .build()
            );
        }

        return sleepLogMetrics;
    }

    public List<FitbitSleepSummaryLogMetricDto> getSleepSummaryMetrics(List<FitbitSleepSummaryLog> sleepSummaryLogs) {
        List<FitbitSleepSummaryLogMetricDto> sleepSummaries = new ArrayList<>();
        if (sleepSummaryLogs == null || sleepSummaryLogs.isEmpty()) {
            return sleepSummaries;
        }

        for (int i = 0; i < sleepSummaryLogs.size(); i++) {
            FitbitSleepSummaryLog log = sleepSummaryLogs.get(i);
            if (log == null || log.getTotalMinutesAsleep() == null) continue;
            sleepSummaries.add(FitbitSleepSummaryLogMetricDto.builder()
                    .totalMinutesAsleep(log.getTotalMinutesAsleep())
                    .build()
            );
        }

        return sleepSummaries;
    }

    //Helper functions to calculate Fitbit metrics:

    public int calculateAverageRestingHeartRate(List<Integer> restingHeartRates) {
        if (restingHeartRates == null || restingHeartRates.isEmpty()) {
            return -1;
        }

        int sum = 0;
        int count = 0;
        for (int i = 0; i < restingHeartRates.size(); i++) {
            if (restingHeartRates.get(i) == null) continue;
            sum = sum + restingHeartRates.get(i);
            count++;
        }

        if (count == 0) {
            return -1;
        }

        return sum / count;
    }

    public int calculateAverageCaloriesOut(List<FitbitActivitySummariesMetricDto> activityMetrics) {
        if (activityMetrics == null || activityMetrics.isEmpty()) {
            return -1;
        }

        int sum = 0;
        int count = 0;

        for (int i = 0; i < activityMetrics.size(); i++) {
            FitbitActivitySummariesMetricDto metric = activityMetrics.get(i);
            if (metric == null || metric.getCaloriesOut() == null) continue;
            sum = sum + metric.getCaloriesOut();
            count++;
        }

        if (count == 0) return -1;

        return sum / count;
    }

    public int calculateAverageSteps(List<FitbitActivitySummariesMetricDto> activityMetrics) {
        if (activityMetrics == null || activityMetrics.isEmpty()) {
            return -1;
        }

        int sum = 0;
        int count = 0;

        for (int i = 0; i < activityMetrics.size(); i++) {
            FitbitActivitySummariesMetricDto metric = activityMetrics.get(i);
            if (metric == null || metric.getSteps() == null) continue;
            sum = sum + metric.getSteps();
            count++;
        }

        if (count == 0) return -1;

        return sum / count;
    }

    public int calculateAverageSedentaryMinutes(List<FitbitActivitySummariesMetricDto> activityMetrics) {
        if (activityMetrics == null || activityMetrics.isEmpty()) {
            return -1;
        }

        int sum = 0;
        int count = 0;

        for (int i = 0; i < activityMetrics.size(); i++) {
            FitbitActivitySummariesMetricDto metric = activityMetrics.get(i);
            if (metric == null || metric.getSedentaryMinutes() == null) continue;
            sum = sum + metric.getSedentaryMinutes();
            count++;
        }

        if (count == 0) return -1;

        return sum / count;
    }

    public int calculateAverageActiveMinutes(List<FitbitActivityGoalsLogMetricDto> activityGoalsLogMetrics) {
        if (activityGoalsLogMetrics == null || activityGoalsLogMetrics.isEmpty()) {
            return -1;
        }

        int sum = 0;
        int count = 0;

        for (int i = 0; i < activityGoalsLogMetrics.size(); i++) {
            FitbitActivityGoalsLogMetricDto metricDto = activityGoalsLogMetrics.get(i);
            if (metricDto == null || metricDto.getActiveMinutes() == null) continue;
            sum = sum + metricDto.getActiveMinutes();
            count++;
        }

        if (count == 0) return -1;
        return sum / count;
    }

    public int calculateAverageEfficiency(List<FitbitSleepLogMetricDto> sleepLogMetrics) {
        if (sleepLogMetrics == null || sleepLogMetrics.isEmpty()) {
            return -1;
        }

        int sum = 0;
        int count = 0;

        for (int i = 0; i < sleepLogMetrics.size(); i++) {
            FitbitSleepLogMetricDto metric = sleepLogMetrics.get(i);
            if (metric == null || metric.getEfficiency() == null) continue;
            sum = sum + metric.getEfficiency();
            count++;
        }

        if (count == 0) return -1;

        return sum / count;
    }

    public int calculateAverageTotalMinutesAsleep(List<FitbitSleepSummaryLogMetricDto> sleepSummaryMetrics) {
        if (sleepSummaryMetrics == null || sleepSummaryMetrics.isEmpty()) {
            return -1;
        }

        int sum = 0;
        int count = 0;

        for (int i = 0; i < sleepSummaryMetrics.size(); i++) {
            FitbitSleepSummaryLogMetricDto metric = sleepSummaryMetrics.get(i);
            if (metric == null || metric.getTotalMinutesAsleep() == null) continue;
            sum = sum + metric.getTotalMinutesAsleep();
            count++;
        }

        if (count == 0) return -1;
        return sum / count;
    }

    //Helper functions to calculate average of Manual log:
    public int calculateAverageSleepingDuration(List<ManualDailyLog> manualDailyLogs) {
        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return -1;
        }

        int sum = 0;
        int count = 0;

        for (int i = 0; i < manualDailyLogs.size(); i++) {
            ManualDailyLog log = manualDailyLogs.get(i);

            if (log == null) continue;

            int score = EnumScoreHelper.sleepDuration(log.getSleepDuration());
            if (score < 0) continue;
            sum = sum + score;
            count++;
        }

        return count == 0 ? -1 : sum / count;
    }

    public int calculateAverageNightWakeUps(List<ManualDailyLog> manualDailyLogs) {
        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return -1;
        }

        int sum = 0;
        int count = 0;

        for (int i = 0; i < manualDailyLogs.size(); i++) {
            ManualDailyLog log = manualDailyLogs.get(i);
            if (log == null) continue;

            int score = EnumScoreHelper.nightWakeUps(log.getNightWakeUps());
            if (score < 0) continue;
            sum = sum + score;
            count++;
        }

        return count == 0 ? -1 : sum / count;
    }

    public int calculateAverageManualRestingHeartRate(List<ManualDailyLog> manualDailyLogs) {
        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return -1;
        }

        int sum = 0;
        int count = 0;

        for (int i = 0; i < manualDailyLogs.size(); i++) {
            ManualDailyLog log = manualDailyLogs.get(i);
            if (log == null) continue;

            Integer restingHeartRate = log.getRestingHeartRate();

            if (restingHeartRate == null || restingHeartRate <= 0) {
                continue;
            }

            sum = sum + restingHeartRate;
            count++;
        }

        return count == 0 ? -1 : sum / count;
    }

    public int calculateAveragePainLevel(List<ManualDailyLog> manualDailyLogs) {
        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return -1;
        }

        int sum = 0;
        int count = 0;

        for (int i = 0; i < manualDailyLogs.size(); i++) {
            ManualDailyLog log = manualDailyLogs.get(i);
            if (log == null) continue;
            int score = EnumScoreHelper.pain(log.getPainLevel());
            if (score < 0) continue;
            sum = sum + score;
            count++;
        }

        return count == 0 ? -1 : sum / count;
    }

    public int calculateAverageSittingTime(List<ManualDailyLog> manualDailyLogs) {
        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return -1;
        }

        int sum = 0;
        int count = 0;

        for (int i = 0; i < manualDailyLogs.size(); i++) {
            ManualDailyLog log = manualDailyLogs.get(i);
            if (log == null) continue;
            int score = EnumScoreHelper.sittingTime(log.getSittingTime());
            if (score < 0) continue;
            sum = sum + score;
            count++;
        }

        return count == 0 ? -1 : sum / count;
    }

    public int calculateAverageStandingTime(List<ManualDailyLog> manualDailyLogs) {
        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return -1;
        }

        int sum = 0;
        int count = 0;

        for (int i = 0; i < manualDailyLogs.size(); i++) {
            ManualDailyLog log = manualDailyLogs.get(i);
            if (log == null) continue;
            int score = EnumScoreHelper.standingTime(log.getStandingTime());
            if (score < 0) continue;
            sum = sum + score;
            count++;
        }

        return count == 0 ? -1 : sum / count;
    }

    public int calculateAverageMorningStiffness(List<ManualDailyLog> manualDailyLogs) {
        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return -1;
        }

        int sum = 0;
        int count = 0;

        for (int i = 0; i < manualDailyLogs.size(); i++) {
            ManualDailyLog log = manualDailyLogs.get(i);
            if (log == null) continue;
            int score = EnumScoreHelper.morningStiffness(log.getMorningStiffness());
            if (score < 0) continue;
            sum = sum + score;
            count++;
        }

        return count == 0 ? -1 : sum / count;
    }

    public int calculateAverageStressLevel(List<ManualDailyLog> manualDailyLogs) {
        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return -1;
        }

        int sum = 0;
        int count = 0;

        for (int i = 0; i < manualDailyLogs.size(); i++) {
            ManualDailyLog log = manualDailyLogs.get(i);
            if (log == null) continue;
            int score = EnumScoreHelper.stressLevel(log.getStressLevel());
            if (score < 0) continue;
            sum = sum + score;
            count++;
        }

        return count == 0 ? -1 : sum / count;
    }

    public int calculateDaysWithStretching(List<ManualDailyLog> manualDailyLogs) {
        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return -1;
        }

        int daysWithStretching = 0;

        for (int i = 0; i < manualDailyLogs.size(); i++) {
            ManualDailyLog metric = manualDailyLogs.get(i);
            if (metric == null || metric.getStretchingDone() == null) continue;
            if (Boolean.TRUE.equals(metric.getStretchingDone())) {
                daysWithStretching++;
            }
        }

        return daysWithStretching;
    }

    public int calculateDaysWithFlareUp(List<ManualDailyLog> manualDailyLogs) {
        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return -1;
        }

        int daysWithFlareUps = 0;

        for (int i = 0; i < manualDailyLogs.size(); i++) {
            ManualDailyLog metric = manualDailyLogs.get(i);
            if (metric == null || metric.getFlareUpToday() == null) continue;
            if (Boolean.TRUE.equals(metric.getFlareUpToday())) {
                daysWithFlareUps++;
            }
        }

        return daysWithFlareUps;
    }

    public int calculateDaysWithNumbnessTingling(List<ManualDailyLog> manualDailyLogs) {
        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return -1;
        }

        int daysWithNumbnessTingling = 0;

        for (int i = 0; i < manualDailyLogs.size(); i++) {
            ManualDailyLog metric = manualDailyLogs.get(i);
            if (metric == null || metric.getNumbnessTingling() == null) continue;
            if (Boolean.TRUE.equals(metric.getNumbnessTingling())) {
                daysWithNumbnessTingling++;
            }
        }

        return daysWithNumbnessTingling;
    }

    public int calculateDaysWithLiftingOrStrain(List<ManualDailyLog> manualDailyLogs) {
        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return -1;
        }

        int daysWithLiftingOrStrain = 0;

        for (int i = 0; i < manualDailyLogs.size(); i++) {
            ManualDailyLog metric = manualDailyLogs.get(i);
            if (metric == null || metric.getLiftingOrStrain() == null) continue;
            if (Boolean.TRUE.equals(metric.getLiftingOrStrain())) {
                daysWithLiftingOrStrain++;
            }
        }

        return daysWithLiftingOrStrain;
    }

    //Calculate the windowDays:
    public int calculateDaysAvailable(
            List<ManualDailyLog> manualDailyLogs,
            List<FitbitActivitiesHeartLog> heartLogs,
            List<FitbitActivitySummariesLog> activitySummariesLogs,
            List<FitbitActivityGoalsLog> activityGoalsLogs,
            List<FitbitSleepLog> sleepLogs,
            List<FitbitSleepSummaryLog> sleepSummaryLogs
    ) {
        Set<LocalDate> uniqueDates = new HashSet<>();

        if (manualDailyLogs != null && !manualDailyLogs.isEmpty()) {
            for (int i = 0; i < manualDailyLogs.size(); i++) {
                ManualDailyLog log = manualDailyLogs.get(i);
                if (log == null || log.getLogDate() == null) continue;
                uniqueDates.add(log.getLogDate());
            }
        }

        if (heartLogs != null && !heartLogs.isEmpty()) {
            for (int i = 0; i < heartLogs.size(); i++) {
                FitbitActivitiesHeartLog log = heartLogs.get(i);
                if (log == null || log.getLogDate() == null) continue;
                uniqueDates.add(log.getLogDate());
            }
        }

        if (activitySummariesLogs != null && !activitySummariesLogs.isEmpty()) {
            for (int i = 0; i < activitySummariesLogs.size(); i++) {
                FitbitActivitySummariesLog log = activitySummariesLogs.get(i);
                if (log == null || log.getLogDate() == null) continue;
                uniqueDates.add(log.getLogDate());
            }
        }

        if (activityGoalsLogs != null && !activityGoalsLogs.isEmpty()) {
            for (int i = 0; i < activityGoalsLogs.size(); i++) {
                FitbitActivityGoalsLog log = activityGoalsLogs.get(i);
                if (log == null || log.getLogDate() == null) continue;
                uniqueDates.add(log.getLogDate());
            }
        }

        if (sleepLogs != null && !sleepLogs.isEmpty()) {
            for (int i = 0; i < sleepLogs.size(); i++) {
                FitbitSleepLog log = sleepLogs.get(i);
                if (log == null || log.getLogDate() == null) continue;
                uniqueDates.add(log.getLogDate());
            }
        }

        if (sleepSummaryLogs != null && !sleepSummaryLogs.isEmpty()) {
            for (int i = 0; i < sleepSummaryLogs.size(); i++) {
                FitbitSleepSummaryLog log = sleepSummaryLogs.get(i);
                if (log == null || log.getLogDate() == null) continue;
                uniqueDates.add(log.getLogDate());
            }
        }

        return uniqueDates.size();
    }

    //Risk forecast:
    public int getYesterdaysSleep(List<FitbitSleepSummaryLog> sleepSummaryLogs) {
        if (sleepSummaryLogs == null || sleepSummaryLogs.isEmpty()) {
            return -1;
        }

        //Sort the list in ascending order of logDate:
        sleepSummaryLogs.sort(Comparator.comparing(FitbitSleepSummaryLog::getLogDate));

        FitbitSleepSummaryLog log = sleepSummaryLogs.get(sleepSummaryLogs.size() - 1);

        if (log == null || log.getTotalMinutesAsleep() == null) {
            return -1;
        }

        return log.getTotalMinutesAsleep();
    }

    public int getYesterdaysRestingHeartRate(List<FitbitActivitiesHeartLog> heartLogs) {
        if (heartLogs == null || heartLogs.isEmpty()) {
            return -1;
        }

        //Sort the list in ascending order of logDate:
        heartLogs.sort(Comparator.comparing(FitbitActivitiesHeartLog::getLogDate));

        FitbitActivitiesHeartLog log = heartLogs.get(heartLogs.size() - 1);

        if (log == null || log.getValues() == null || log.getValues().isEmpty()) {
            return -1;
        }

        List<FitbitActivitiesHeartValueLog> values = log.getValues();
        FitbitActivitiesHeartValueLog restingHeartRateLog = values.get(values.size() - 1);

        if (restingHeartRateLog == null || restingHeartRateLog.getRestingHeartRate() == null) {
            return -1;
        }

        return restingHeartRateLog.getRestingHeartRate();
    }

    public int getYesterdaysPainLevel(List<ManualDailyLog> manualDailyLogs) {
        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return -1;
        }

        //Sort the list in ascending order of logDate:
        manualDailyLogs.sort(Comparator.comparing(ManualDailyLog::getLogDate));

        ManualDailyLog log = manualDailyLogs.get(manualDailyLogs.size() - 1);

        if (log == null || log.getPainLevel() == null) {
            return -1;
        }

        return EnumScoreHelper.pain(log.getPainLevel());
    }

    public int getYesterdaysManualRestingHeartRate(List<ManualDailyLog> manualDailyLogs) {
        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return -1;
        }

        //Sort the list in ascending order of logDate:
        manualDailyLogs.sort(Comparator.comparing(ManualDailyLog::getLogDate));

        ManualDailyLog log = manualDailyLogs.get(manualDailyLogs.size() - 1);

        if (log == null || log.getRestingHeartRate() == null) {
            return -1;
        }

        return log.getRestingHeartRate();
    }

    public int getYesterdaysSleepDuration(List<ManualDailyLog> manualDailyLogs) {
        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return -1;
        }

        //Sort the list in ascending order of logDate:
        manualDailyLogs.sort(Comparator.comparing(ManualDailyLog::getLogDate));

        ManualDailyLog log = manualDailyLogs.get(manualDailyLogs.size() - 1);

        if (log == null || log.getSleepDuration() == null) {
            return -1;
        }

        return EnumScoreHelper.sleepDuration(log.getSleepDuration());
    }

    public int getYesterdaysNightWakeUps(List<ManualDailyLog> manualDailyLogs) {
        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return -1;
        }

        //Sort the list in ascending order of logDate:
        manualDailyLogs.sort(Comparator.comparing(ManualDailyLog::getLogDate));

        ManualDailyLog log = manualDailyLogs.get(manualDailyLogs.size() - 1);

        if (log == null || log.getNightWakeUps() == null) {
            return -1;
        }

        return EnumScoreHelper.nightWakeUps(log.getNightWakeUps());
    }

    public int calculateDaysSinceLastFlareUp(List<ManualDailyLog> manualDailyLogs) {
        if (manualDailyLogs == null || manualDailyLogs.isEmpty()) {
            return -1;
        }

        int daysSinceLastFlareUps = 0;

        //Sort the list in ascending order of logDate:
        manualDailyLogs.sort(Comparator.comparing(ManualDailyLog::getLogDate));

        for (int i = manualDailyLogs.size() - 1; i >= 0; i--) {
            ManualDailyLog log = manualDailyLogs.get(i);
            if (log == null || log.getFlareUpToday() == null) continue;
            if (Boolean.TRUE.equals(log.getFlareUpToday())) {
                return daysSinceLastFlareUps;
            } else {
                daysSinceLastFlareUps++;
            }
        }

        return daysSinceLastFlareUps;
    }

    //Standard Deviations:
    public int calculateStepsStandardDeviation(List<FitbitActivitySummariesLog> activitySummariesLogs) {
        if (activitySummariesLogs == null || activitySummariesLogs.isEmpty()) {
            return -1;
        }

        int sum = 0;
        int count = 0;

        //Calculate mean:
        for (int i = 0; i < activitySummariesLogs.size(); i++) {
            FitbitActivitySummariesLog log = activitySummariesLogs.get(i);
            if (log == null || log.getSteps() == null) continue;
            sum = sum + log.getSteps();
            count++;
        }

        if (count == 0) return -1;

        double mean = (double) sum / count;
        double varianceSum = 0.0;
        int validCount = 0;

        //Calculate variance (average of squared differences from the mean):
        for (int i = 0; i < activitySummariesLogs.size(); i++) {
            FitbitActivitySummariesLog log = activitySummariesLogs.get(i);
            if (log == null || log.getSteps() == null) continue;
            double diff = log.getSteps() - mean;
            varianceSum += diff * diff;
            validCount++;
        }

        if (validCount == 0) return -1;

        //Compute variance and standard deviation:
        double variance = varianceSum / validCount;
        return (int) Math.round(Math.sqrt(variance));
    }

    public int calculateRestingHeartRateStandardDeviation(List<FitbitActivitiesHeartLog> heartLogs) {
        if (heartLogs == null || heartLogs.isEmpty()) {
            return -1;
        }

        List<Integer> restingHeartRateArray = new ArrayList<>();

        //Add resting heart rate in to the array:
        for (int i = 0; i < heartLogs.size(); i++) {
            FitbitActivitiesHeartLog log = heartLogs.get(i);
            if (log == null || log.getValues() == null || log.getValues().isEmpty()) continue;
            List<FitbitActivitiesHeartValueLog> values = log.getValues();
            for (int j = 0; j < values.size(); j++) {
                FitbitActivitiesHeartValueLog heartValueLog = values.get(j);
                if (heartValueLog == null || heartValueLog.getRestingHeartRate() == null) continue;
                restingHeartRateArray.add(heartValueLog.getRestingHeartRate());
            }
        }

        if (restingHeartRateArray.isEmpty()) return -1;

        int sum = 0;
        //Calculate mean (average):
        for (int i = 0; i < restingHeartRateArray.size(); i++) {
            sum = sum + restingHeartRateArray.get(i);
        }

        double mean = (double) sum / restingHeartRateArray.size();

        //Calculate variance:
        double varianceSum = 0.0;
        for (int i = 0; i < restingHeartRateArray.size(); i++) {
            double diff = restingHeartRateArray.get(i) - mean;
            varianceSum += diff * diff;
        }

        double variance = varianceSum / restingHeartRateArray.size();

        //Calculate standard deviation (sqrt of variance):
        double stdDev = Math.sqrt(variance);

        //Return rounded integer
        return (int) Math.round(stdDev);
    }

    public int calculateSleepStandardDeviation(List<FitbitSleepSummaryLog> sleepSummaryLogs) {
        if (sleepSummaryLogs == null || sleepSummaryLogs.isEmpty()) {
            return -1;
        }

        List<Integer> sleepMinutesArray = new ArrayList<>();

        for (int i = 0; i < sleepSummaryLogs.size(); i++) {
            FitbitSleepSummaryLog log = sleepSummaryLogs.get(i);
            if (log == null || log.getTotalMinutesAsleep() == null) continue;
            sleepMinutesArray.add(log.getTotalMinutesAsleep());
        }

        if (sleepMinutesArray.isEmpty()) return -1;

        //Calculate mean (average):
        int sum = 0;
        for (int i = 0; i < sleepMinutesArray.size(); i++) {
            sum = sum + sleepMinutesArray.get(i);
        }

        double mean = (double) sum / sleepMinutesArray.size();

        //Calculate variance:
        double varianceSum = 0.0;
        for (int i = 0; i < sleepMinutesArray.size(); i++) {
            double diff = sleepMinutesArray.get(i) - mean;
            varianceSum += diff * diff;
        }

        double variance = varianceSum / sleepMinutesArray.size();

        //Calculate standard deviation:
        double stdDev = Math.sqrt(variance);

        return (int) Math.round(stdDev);
    }

    public int calculateSedentaryStandardDeviation(List<FitbitActivitySummariesLog> activitySummariesLogs) {
        if (activitySummariesLogs == null || activitySummariesLogs.isEmpty()) {
            return -1;
        }

        List<Integer> sedentaryMinutesArray = new ArrayList<>();

        for (int i = 0; i < activitySummariesLogs.size(); i++) {
            FitbitActivitySummariesLog log = activitySummariesLogs.get(i);
            if (log == null || log.getSedentaryMinutes() == null) continue;
            sedentaryMinutesArray.add(log.getSedentaryMinutes());
        }

        //Calculate mean (average):
        int sum = 0;
        for (int i = 0; i < sedentaryMinutesArray.size(); i++) {
            sum = sum + sedentaryMinutesArray.get(i);
        }

        double mean = (double) sum / sedentaryMinutesArray.size();

        //Calculate variance:
        double varianceSum = 0.0;
        for (int i = 0; i < sedentaryMinutesArray.size(); i++) {
            double diff = sedentaryMinutesArray.get(i) - mean;
            varianceSum += diff * diff;
        }
        double variance = varianceSum / sedentaryMinutesArray.size();

        //Calculate standard deviation:
        double stdDev = Math.sqrt(variance);

        return (int) Math.round(stdDev);
    }
}
