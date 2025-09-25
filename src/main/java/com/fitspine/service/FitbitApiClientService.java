package com.fitspine.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fitspine.client.FitbitApiClient;
import com.fitspine.exception.UserNotFoundException;
import com.fitspine.model.*;
import com.fitspine.repository.*;
import com.fitspine.service.impl.FitbitServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class FitbitApiClientService {
    @Value("${FITBIT_CLIENT_ID}")
    private String clientId;

    @Value("${FITBIT_CLIENT_SECRET}")
    private String clientSecret;

    private final FitbitApiClient fitbitApiClient;
    private final UserRepository userRepository;
    private FitbitServiceImpl fitbitService;

    private final FitbitActivitiesHeartLogRepository fitbitActivitiesHeartLogRepository;

    private final FitbitActivitiesHeartValueLogRepository fitbitActivitiesHeartValueLogRepository;

    private final FitbitActivitiesHeartValueHeartRateZonesLogRepository fitbitActivitiesHeartValueHeartRateZonesLogRepository;
    private final FitbitActivitiesLogRepository fitbitActivitiesLogRepository;
    private final FitbitActivitySummariesLogRepository fitbitActivitySummariesLogRepository;
    private final FitbitActivitySummariesDistancesLogRepository fitbitActivitySummariesDistancesLogRepository;
    private final FitbitActivityGoalsLogRepository fitbitActivityGoalsLogRepository;

    private final FitbitSleepSummaryLogRepository fitbitSleepSummaryLogRepository;
    private final FitbitSleepSummaryStagesLogRepository fitbitSleepSummaryStagesLogRepository;
    private final FitbitSleepLogRepository fitbitSleepLogRepository;
    private final FitbitSleepDataLogRepository fitbitSleepDataLogRepository;
    private final FitbitSleepShortDataLogRepository fitbitSleepShortDataLogRepository;

    public FitbitApiClientService(FitbitApiClient fitbitApiClient,
                                  UserRepository userRepository,
                                  FitbitActivitiesHeartLogRepository fitbitActivitiesHeartLogRepository,
                                  FitbitActivitiesHeartValueLogRepository fitbitActivitiesHeartValueLogRepository,
                                  FitbitActivitiesHeartValueHeartRateZonesLogRepository fitbitActivitiesHeartValueHeartRateZonesLogRepository,
                                  FitbitActivitiesLogRepository fitbitActivitiesLogRepository,
                                  FitbitActivitySummariesLogRepository fitbitActivitySummariesLogRepository,
                                  FitbitActivitySummariesDistancesLogRepository fitbitActivitySummariesDistancesLogRepository,
                                  FitbitActivityGoalsLogRepository fitbitActivityGoalsLogRepository,
                                  FitbitSleepSummaryLogRepository fitbitSleepSummaryLogRepository,
                                  FitbitSleepSummaryStagesLogRepository fitbitSleepSummaryStagesLogRepository,
                                  FitbitSleepLogRepository fitbitSleepLogRepository,
                                  FitbitSleepDataLogRepository fitbitSleepDataLogRepository,
                                  FitbitSleepShortDataLogRepository fitbitSleepShortDataLogRepository,
                                  FitbitServiceImpl fitbitService) {
        this.fitbitApiClient = fitbitApiClient;
        this.userRepository = userRepository;
        this.fitbitActivitiesHeartLogRepository = fitbitActivitiesHeartLogRepository;
        this.fitbitActivitiesHeartValueLogRepository = fitbitActivitiesHeartValueLogRepository;
        this.fitbitActivitiesHeartValueHeartRateZonesLogRepository = fitbitActivitiesHeartValueHeartRateZonesLogRepository;
        this.fitbitActivitiesLogRepository = fitbitActivitiesLogRepository;
        this.fitbitActivitySummariesLogRepository = fitbitActivitySummariesLogRepository;
        this.fitbitActivitySummariesDistancesLogRepository = fitbitActivitySummariesDistancesLogRepository;
        this.fitbitActivityGoalsLogRepository = fitbitActivityGoalsLogRepository;
        this.fitbitSleepSummaryLogRepository = fitbitSleepSummaryLogRepository;
        this.fitbitSleepSummaryStagesLogRepository = fitbitSleepSummaryStagesLogRepository;
        this.fitbitSleepLogRepository = fitbitSleepLogRepository;
        this.fitbitSleepDataLogRepository = fitbitSleepDataLogRepository;
        this.fitbitSleepShortDataLogRepository = fitbitSleepShortDataLogRepository;
        this.fitbitService = fitbitService;
    }

    @Transactional
    public JsonNode getSteps(String email, String date) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User with email: " + email + " not found"));
        log.info("Fetching Fitbit steps for user {} on date {}", user.getId(), date);
        JsonNode root = fitbitApiClient.getSteps(user.getId(), clientId, clientSecret, date);

        JsonNode activities = root.get("activities");
        JsonNode summary = root.get("summary");
        JsonNode goals = root.get("goals");

        if ((activities == null || activities.isEmpty()) && (summary == null || summary.isEmpty()) && (goals == null || goals.isEmpty())) {
            log.info("No activities data found for user {} on date {}: ", user.getId(), date);
            return root;
        }

        //Save Activities:
        if (activities != null && activities.isArray()) {
            for (JsonNode activity : activities) {
                FitbitActivitiesLog activitiesLog = new FitbitActivitiesLog();
                activitiesLog.setUser(user);
                activitiesLog.setProvider(fitbitService.getProvider());
                activitiesLog.setLogDate(LocalDate.parse(date));
                activitiesLog.setLogId(activity.get("logId").asLong());
                activitiesLog.setActivityId(activity.get("activityId").asInt());
                activitiesLog.setActivityParentId(activity.get("activityParentId").asInt());
                activitiesLog.setActivityParentName(activity.get("activityParentName").asText());
                activitiesLog.setName(activity.get("name").asText());
                activitiesLog.setDescription(activity.get("description").asText());
                activitiesLog.setCalories(activity.get("calories").asInt());
                activitiesLog.setDistance(activity.get("distance").asDouble());
                activitiesLog.setSteps(activity.get("steps").asInt());
                activitiesLog.setDuration(activity.get("duration").asLong());
                OffsetDateTime lastModified = OffsetDateTime.parse(activity.get("lastModified").asText(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                activitiesLog.setLastModified(lastModified.toLocalDateTime());
                activitiesLog.setStartTime(LocalTime.parse(activity.get("startTime").asText()));
                activitiesLog.setFavourite(activity.get("isFavorite").asBoolean());
                activitiesLog.setHasActiveZoneMinutes(activity.get("hasActiveZoneMinutes").asBoolean());
                activitiesLog.setStartDate(LocalDate.parse(activity.get("startDate").asText()));
                activitiesLog.setHasStartTime(activity.get("hasStartTime").asBoolean());
                fitbitActivitiesLogRepository.save(activitiesLog);
            }
        }

        //Save Goals:
        if (goals != null && goals.isObject()) {
            FitbitActivityGoalsLog goalsLog = new FitbitActivityGoalsLog();
            goalsLog.setUser(user);
            goalsLog.setProvider(fitbitService.getProvider());
            goalsLog.setLogDate(LocalDate.parse(date));
            goalsLog.setCaloriesOut(goals.get("caloriesOut").asInt());
            goalsLog.setSteps(goals.get("steps").asInt());
            goalsLog.setDistance(goals.get("distance").asDouble());
            goalsLog.setFloors(goals.get("floors").asInt());
            goalsLog.setActiveMinutes(goals.get("activeMinutes").asInt());

            fitbitActivityGoalsLogRepository.save(goalsLog);
        }

        //Save Summary and distances:
        if (summary != null && summary.isObject()) {
            FitbitActivitySummariesLog summariesLog = new FitbitActivitySummariesLog();
            summariesLog.setUser(user);
            summariesLog.setProvider(fitbitService.getProvider());
            summariesLog.setLogDate(LocalDate.parse(date));
            summariesLog.setCaloriesOut(summary.get("caloriesOut").asInt());
            summariesLog.setActivityCalories(summary.get("activityCalories").asInt());
            summariesLog.setCaloriesBmr(summary.get("caloriesBMR").asInt());
            summariesLog.setActiveScore(summary.get("activeScore").asInt());
            summariesLog.setSteps(summary.get("steps").asInt());
            summariesLog.setSedentaryMinutes(summary.get("sedentaryMinutes").asInt());
            summariesLog.setLightlyActiveMinutes(summary.get("lightlyActiveMinutes").asInt());
            summariesLog.setFairlyActiveMinutes(summary.get("fairlyActiveMinutes").asInt());
            summariesLog.setVeryActiveMinutes(summary.get("veryActiveMinutes").asInt());
            summariesLog.setMarginalCalories(summary.get("marginalCalories").asInt());
            summariesLog.setRawJson(summary.toString());
            FitbitActivitySummariesLog savedSummary = fitbitActivitySummariesLogRepository.save(summariesLog);

            JsonNode distances = summary.get("distances");
            if (distances != null && distances.isArray()) {
                for (JsonNode d : distances) {
                    FitbitActivitySummariesDistancesLog distancesLog = new FitbitActivitySummariesDistancesLog();
                    distancesLog.setFitbitActivitySummariesLog(savedSummary);
                    distancesLog.setActivity(d.get("activity").asText());
                    distancesLog.setDistance(d.get("distance").asDouble());
                    fitbitActivitySummariesDistancesLogRepository.save(distancesLog);
                }
            }
        }

        return root;
    }

    @Transactional
    public JsonNode getSleep(String email, String date) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User with email: " + email + " not found"));
        log.info("Fetching Fitbit sleep for user {} on date {}", user.getId(), date);
        JsonNode root = fitbitApiClient.getSleep(user.getId(), clientId, clientSecret, date);

        JsonNode sleep = root.get("sleep");
        JsonNode summary = root.get("summary");

        if ((sleep == null || sleep.isEmpty()) && (summary == null || summary.isEmpty())) {
            log.warn("No sleep data found for user {} on date {}: ", user.getId(), date);
            return root;
        }

        //Save sleep:
        if (sleep != null && sleep.isArray()) {
            for (JsonNode s : sleep) {
                Long logId = s.get("logId").asLong();

                if (fitbitSleepLogRepository.existsByUserAndLogId(user, logId)) {
                    log.info("Skipping duplicate sleep log {} for user {}", logId, user.getId());
                    continue;
                }

                FitbitSleepLog sleepLog = new FitbitSleepLog();
                sleepLog.setUser(user);
                sleepLog.setProvider(fitbitService.getProvider());
                sleepLog.setLogDate(LocalDate.parse(date));
                sleepLog.setDateOfSleep(LocalDate.parse(s.get("dateOfSleep").asText()));
                sleepLog.setEfficiency(s.has("efficiency") ? s.get("efficiency").asInt() : null);
                sleepLog.setStartTime(LocalDateTime.parse(s.get("startTime").asText()));
                sleepLog.setEndTime(LocalDateTime.parse(s.get("endTime").asText()));
                sleepLog.setInfoCode(s.has("infoCode") ? s.get("infoCode").asInt() : null);
                sleepLog.setIsMainSleep(s.has("isMainSleep") && s.get("isMainSleep").asBoolean());
                sleepLog.setLogId(s.has("logId") ? s.get("logId").asLong() : null);
                sleepLog.setMinutesAfterWakeup(s.has("minutesAfterWakeup") ? s.get("minutesAfterWakeup").asInt() : null);
                sleepLog.setMinutesAwake(s.has("minutesAwake") ? s.get("minutesAwake").asInt() : null);
                sleepLog.setMinutesAsleep(s.has("minutesAsleep") ? s.get("minutesAsleep").asInt() : null);
                sleepLog.setMinutesToFallAsleep(s.has("minutesToFallAsleep") ? s.get("minutesToFallAsleep").asInt() : null);
                sleepLog.setLogType(s.has("logType") ? s.get("logType").asText() : null);
                sleepLog.setTimeInBed(s.has("timeInBed") ? s.get("timeInBed").asInt() : null);
                sleepLog.setType(s.has("type") ? s.get("type").asText() : null);
                FitbitSleepLog savedSleepLog = fitbitSleepLogRepository.save(sleepLog);

                //Save sleep data:
                JsonNode levels = s.get("levels");
                if (levels != null && levels.has("data")) {
                    for (JsonNode d : levels.get("data")) {
                        FitbitSleepDataLog dataLog = new FitbitSleepDataLog();
                        dataLog.setFitbitSleepLog(savedSleepLog);
                        dataLog.setDateTime(LocalDateTime.parse(d.get("dateTime").asText()));
                        dataLog.setLevel(d.get("level").asText());
                        dataLog.setSeconds(d.get("seconds").asInt());
                        fitbitSleepDataLogRepository.save(dataLog);
                    }
                }

                //Save sleep short data:
                if (levels != null && levels.has("shortData")) {
                    for (JsonNode sd : levels.get("shortData")) {
                        FitbitSleepShortDataLog shortDataLog = new FitbitSleepShortDataLog();
                        shortDataLog.setFitbitSleepLog(savedSleepLog);
                        shortDataLog.setDateTime(LocalDateTime.parse(sd.get("dateTime").asText()));
                        shortDataLog.setLevel(sd.get("level").asText());
                        shortDataLog.setSeconds(sd.get("seconds").asInt());
                        fitbitSleepShortDataLogRepository.save(shortDataLog);
                    }
                }
            }
        }


        //Save Summary:
        if (summary != null && summary.isObject()) {
            LocalDate logDate = LocalDate.parse(date);

            if (fitbitSleepSummaryLogRepository.existsByUserAndLogDate(user, logDate)) {
                log.info("Skipping duplicate sleep summary for user {} on {}: ", user.getId(), logDate);
                return root;
            }

            FitbitSleepSummaryLog sleepSummaryLog = new FitbitSleepSummaryLog();
            sleepSummaryLog.setUser(user);
            sleepSummaryLog.setProvider(fitbitService.getProvider());
            sleepSummaryLog.setLogDate(LocalDate.parse(date));
            sleepSummaryLog.setTotalMinutesAsleep(summary.has("totalMinutesAsleep") ? summary.get("totalMinutesAsleep").asInt() : null);
            sleepSummaryLog.setTotalSleepRecords(summary.has("totalSleepRecords") ? summary.get("totalSleepRecords").asInt() : null);
            sleepSummaryLog.setTotalTimeInBed(summary.has("totalTimeInBed") ? summary.get("totalTimeInBed").asInt() : null);
            sleepSummaryLog.setRawJson(root.toString());
            FitbitSleepSummaryLog savedSleepSummaryLog = fitbitSleepSummaryLogRepository.save(sleepSummaryLog);

            //Extract stages from summary:
            JsonNode stages = summary.get("stages");

            //Save Stages:
            if (stages != null && stages.isObject()) {
                FitbitSleepSummaryStagesLog stagesLog = new FitbitSleepSummaryStagesLog();
                stagesLog.setFitbitSleepSummaryLog(savedSleepSummaryLog);
                stagesLog.setDeep(stages.has("deep") ? stages.get("deep").asInt() : null);
                stagesLog.setLight(stages.has("light") ? stages.get("light").asInt() : null);
                stagesLog.setRem(stages.has("rem") ? stages.get("rem").asInt() : null);
                stagesLog.setWake(stages.has("wake") ? stages.get("wake").asInt() : null);
                fitbitSleepSummaryStagesLogRepository.save(stagesLog);
            }
        }

        return root;
    }

    @Transactional
    public JsonNode getHeartRate(String email, String date) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User with email: " + email + " not found"));
        log.info("Fetching Fitbit heart rate for user {} on date {}", user.getId(), date);
        JsonNode root = fitbitApiClient.getHeartRate(user.getId(), clientId, clientSecret, date);

        JsonNode activitiesHeartArray = root.get("activities-heart");
        if (activitiesHeartArray == null || !activitiesHeartArray.isArray() || activitiesHeartArray.isEmpty()) {
            log.warn("No heart rate data found for user {} on date {}", user.getId(), date);
            return root;
        }

        JsonNode heartEntry = activitiesHeartArray.get(0);
        LocalDate dateTime = LocalDate.parse(heartEntry.get("dateTime").asText());
        JsonNode valueArray = heartEntry.get("value");
        int restingHeartRate = valueArray.get("restingHeartRate").asInt();
        JsonNode heartRateZonesArray = valueArray.get("heartRateZones");

        //Save heart log:
        FitbitActivitiesHeartLog activitiesHeartLog = new FitbitActivitiesHeartLog();
        activitiesHeartLog.setUser(user);
        activitiesHeartLog.setLogDate(LocalDate.now());
        activitiesHeartLog.setProvider(fitbitService.getProvider());
        activitiesHeartLog.setDateTime(dateTime);
        activitiesHeartLog.setRawJson(root.toString());
        fitbitActivitiesHeartLogRepository.save(activitiesHeartLog);

        //Save value log:
        FitbitActivitiesHeartValueLog valueLog = new FitbitActivitiesHeartValueLog();
        valueLog.setFitbitActivitiesHeartLog(activitiesHeartLog);
        valueLog.setRestingHeartRate(restingHeartRate);
        fitbitActivitiesHeartValueLogRepository.save(valueLog);

        //Save heart zones:
        if (heartRateZonesArray != null && heartRateZonesArray.isArray()) {
            for (JsonNode zone : heartRateZonesArray) {
                FitbitActivitiesHeartValueHeartRateZonesLog zonesLog = new FitbitActivitiesHeartValueHeartRateZonesLog();
                zonesLog.setFitbitActivitiesHeartValuesLog(valueLog);
                zonesLog.setName(zone.get("name").asText());
                zonesLog.setMin(zone.get("min").asInt());
                zonesLog.setMax(zone.get("max").asInt());
                zonesLog.setMinutes(zone.get("minutes").asInt());
                zonesLog.setCaloriesOut(zone.get("caloriesOut").asDouble());
                fitbitActivitiesHeartValueHeartRateZonesLogRepository.save(zonesLog);
            }
        }

        return root;
    }
}
