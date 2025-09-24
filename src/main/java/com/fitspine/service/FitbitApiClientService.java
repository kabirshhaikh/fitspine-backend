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

    public FitbitApiClientService(FitbitApiClient fitbitApiClient,
                                  UserRepository userRepository,
                                  FitbitActivitiesHeartLogRepository fitbitActivitiesHeartLogRepository,
                                  FitbitActivitiesHeartValueLogRepository fitbitActivitiesHeartValueLogRepository,
                                  FitbitActivitiesHeartValueHeartRateZonesLogRepository fitbitActivitiesHeartValueHeartRateZonesLogRepository,
                                  FitbitActivitiesLogRepository fitbitActivitiesLogRepository,
                                  FitbitActivitySummariesLogRepository fitbitActivitySummariesLogRepository,
                                  FitbitActivitySummariesDistancesLogRepository fitbitActivitySummariesDistancesLogRepository,
                                  FitbitActivityGoalsLogRepository fitbitActivityGoalsLogRepository,
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
        this.fitbitService = fitbitService;
    }

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

    public JsonNode getSleep(String email, String date) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User with email: " + email + " not found"));
        log.info("Fetching Fitbit sleep for user {} on date {}", user.getId(), date);
        return fitbitApiClient.getSleep(user.getId(), clientId, clientSecret, date);
    }

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
