package com.fitspine.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitspine.exception.TokenNotFoundException;
import com.fitspine.exception.TokenRefreshException;
import com.fitspine.model.User;
import com.fitspine.model.UserWearableToken;
import com.fitspine.repository.UserRepository;
import com.fitspine.repository.UserWearableTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

@Slf4j
@Service
public class TokenManager {
    @Value("${FITBIT_ACCESS_REFRESH_URI}")
    private String refreshUrl;

    private final UserWearableTokenRepository userWearableTokenRepository;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    public TokenManager(UserWearableTokenRepository userWearableTokenRepository, RestTemplate restTemplate, ObjectMapper objectMapper, UserRepository userRepository) {
        this.userWearableTokenRepository = userWearableTokenRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
    }

    public String getValidToken(Long userId, String provider, String clientId, String clientSecret) {
        UserWearableToken token = userWearableTokenRepository.findByUserIdAndProvider(userId, provider)
                .orElseThrow(() -> new TokenNotFoundException("No token found for user: " + userId));


        //If token is still valid in one min buffer, then return the access token:
        if (token.getExpiresAt().isAfter(LocalDateTime.now().plusMinutes(1))) {
            return token.getAccessToken();
        }

        return refreshToken(token, clientId, clientSecret);
    }

    public String refreshToken(UserWearableToken token, String clientId, String clientSecret) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String credentials = clientId + ":" + clientSecret;
        headers.set("Authorization", "Basic " + Base64.getEncoder().encodeToString(credentials.getBytes()));


        String body = "grant_type=refresh_token&refresh_token=" + token.getRefreshToken();
        HttpEntity<String> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(refreshUrl, request, String.class);
            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            log.info("refresh token root {}", jsonNode);

            if (jsonNode.get("access_token") == null) {
                throw new TokenRefreshException("Fitbit did not return a new access token");
            }

            String newAccessToken = jsonNode.get("access_token").asText();
            String newRefreshToken = jsonNode.get("refresh_token").asText();
            int expiresIn = jsonNode.get("expires_in").asInt();
            String tokenType = jsonNode.get("token_type").asText();
            String scope = jsonNode.get("scope").asText();

            token.setAccessToken(newAccessToken);
            token.setRefreshToken(newRefreshToken);
            token.setExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));
            token.setTokenType(tokenType);
            token.setScope(scope);
            userWearableTokenRepository.save(token);

            log.info("Successfully refreshed Fitbit token for user {}", token.getUser().getId());
            return newAccessToken;
        } catch (HttpClientErrorException e) {
            String errorBody = e.getResponseBodyAsString();
            log.info("Fitbit refresh error body: {}", errorBody);

            try {
                JsonNode errorJson = objectMapper.readTree(errorBody);
                log.error("Refresh error: {}", errorJson);

                if (errorJson.has("errors")) {
                    JsonNode error = errorJson.get("errors").get(0);
                    String errorType = error.has("errorType") ? error.get("errorType").asText() : null;
                    String errorMessage = error.has("message") ? error.get("message").asText() : null;

                    if (errorType != null && errorType.equals("invalid_grant")) {
                        //Extract user:
                        User user = token.getUser();

                        //Delete the token
                        userWearableTokenRepository.delete(token);

                        //Now set the users isWearableConnected and wearable type to null:
                        if (user != null) {
                            user.setWearableType(null);
                            user.setIsWearableConnected(false);
                            userRepository.save(user);

                            log.warn("Fitbit token revoked for user with public ID: {}. User must reconnect.", user.getPublicId());
                        }
                    }
                }
            } catch (Exception jsonException) {
                log.error("Failed to parse fitbit error JSON: {}", jsonException.getMessage());
            }

            throw new TokenRefreshException("Fitbit returned an error: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error refreshing token for user {}: {}", token.getUser().getId(), e.getMessage(), e);
            throw new TokenRefreshException("Fitbit returned an error: " + e.getMessage(), e);
        }
    }
}
