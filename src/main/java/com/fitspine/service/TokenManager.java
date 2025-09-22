package com.fitspine.service;

import com.fitspine.exception.TokenNotFoundException;
import com.fitspine.exception.TokenRefreshException;
import com.fitspine.model.UserWearableToken;
import com.fitspine.repository.UserWearableTokenRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
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

    public TokenManager(UserWearableTokenRepository userWearableTokenRepository, RestTemplate restTemplate) {
        this.userWearableTokenRepository = userWearableTokenRepository;
        this.restTemplate = restTemplate;
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

        Map<String, Object> bodyMap;
        try {
            ResponseEntity<Map<String, Object>> response =
                    restTemplate.exchange(refreshUrl, HttpMethod.POST, request, new ParameterizedTypeReference<>() {
                    });
            bodyMap = response.getBody();

            if (!bodyMap.containsKey("access_token")) {
                throw new TokenRefreshException("Fitbit did not return a new access token");
            }
        } catch (Exception e) {
            log.error("Error refreshing token for user {}: {}", token.getUser().getId(), e.getMessage(), e);
            throw new TokenRefreshException("Failed to refresh Fitbit token", e);
        }

        String newAccessToken = (String) bodyMap.get("access_token");
        String newRefreshToken = (String) bodyMap.get("refresh_token");
        Number expiresInNum = (Number) bodyMap.get("expires_in");
        int expiresIn = expiresInNum != null ? expiresInNum.intValue() : 0;


        token.setAccessToken(newAccessToken);
        token.setRefreshToken(newRefreshToken);
        token.setExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));

        userWearableTokenRepository.save(token);

        log.info("Successfully refreshed Fitbit token for user {}", token.getUser().getId());
        return newAccessToken;
    }
}
