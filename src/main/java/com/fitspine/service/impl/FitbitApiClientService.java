package com.fitspine.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fitspine.client.FitbitApiClient;
import com.fitspine.exception.UserNotFoundException;
import com.fitspine.model.User;
import com.fitspine.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FitbitApiClientService {
    @Value("${FITBIT_CLIENT_ID}")
    private String clientId;

    @Value("${FITBIT_CLIENT_SECRET}")
    private String clientSecret;

    private final FitbitApiClient fitbitApiClient;
    private final UserRepository userRepository;

    public FitbitApiClientService(FitbitApiClient fitbitApiClient, UserRepository userRepository) {
        this.fitbitApiClient = fitbitApiClient;
        this.userRepository = userRepository;
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
        return fitbitApiClient.getHeartRate(user.getId(), clientId, clientSecret, date);
    }
}
