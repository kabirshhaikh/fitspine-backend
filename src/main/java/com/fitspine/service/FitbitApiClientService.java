package com.fitspine.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fitspine.client.FitbitApiClient;
import com.fitspine.exception.UserNotFoundException;
import com.fitspine.model.FitbitActivitiesHeartLog;
import com.fitspine.model.FitbitActivitiesHeartValueHeartRateZonesLog;
import com.fitspine.model.FitbitActivitiesHeartValueLog;
import com.fitspine.model.User;
import com.fitspine.repository.FitbitActivitiesHeartLogRepository;
import com.fitspine.repository.FitbitActivitiesHeartValueHeartRateZonesLogRepository;
import com.fitspine.repository.FitbitActivitiesHeartValueLogRepository;
import com.fitspine.repository.UserRepository;
import com.fitspine.service.impl.FitbitServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;

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

    public FitbitApiClientService(FitbitApiClient fitbitApiClient, UserRepository userRepository, FitbitActivitiesHeartLogRepository fitbitActivitiesHeartLogRepository, FitbitActivitiesHeartValueLogRepository fitbitActivitiesHeartValueLogRepository, FitbitActivitiesHeartValueHeartRateZonesLogRepository fitbitActivitiesHeartValueHeartRateZonesLogRepository, FitbitServiceImpl fitbitService) {
        this.fitbitApiClient = fitbitApiClient;
        this.userRepository = userRepository;
        this.fitbitActivitiesHeartLogRepository = fitbitActivitiesHeartLogRepository;
        this.fitbitActivitiesHeartValueLogRepository = fitbitActivitiesHeartValueLogRepository;
        this.fitbitActivitiesHeartValueHeartRateZonesLogRepository = fitbitActivitiesHeartValueHeartRateZonesLogRepository;
        this.fitbitService = fitbitService;
    }

    public JsonNode getSteps(String email, String date) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User with email: " + email + " not found"));
        log.info("Fetching Fitbit steps for user {} on date {}", user.getId(), date);
        return fitbitApiClient.getSteps(user.getId(), clientId, clientSecret, date);
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
