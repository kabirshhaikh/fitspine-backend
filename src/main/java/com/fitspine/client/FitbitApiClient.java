package com.fitspine.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitspine.exception.FitbitClientApiException;
import com.fitspine.service.TokenManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
public class FitbitApiClient {
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final TokenManager tokenManager;

    private static final String PROVIDER = "FITBIT";

    public FitbitApiClient(RestTemplate restTemplate, ObjectMapper objectMapper, TokenManager tokenManager) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.tokenManager = tokenManager;
    }

    private JsonNode getFitBitData(String url, String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        HttpEntity<String> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            log.debug("Fitbit API Response [{}]: {}", url, response.getBody());
            return objectMapper.readTree(response.getBody());
        } catch (HttpClientErrorException e) {
            log.error("Fitbit API call failed [{}]: status={} body={}", url, e.getStatusCode(), e.getResponseBodyAsString());
            throw new FitbitClientApiException("Fitbit API returned an error: " + e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("Error parsing Fitbit response [{}]: {}", url, e.getMessage(), e);
            throw new FitbitClientApiException("Failed to parse Fitbit API response", e);
        }
    }

    //Get daily steps:
    public JsonNode getActivity(Long userId, String clientId, String clientSecret, String date) {
        String accessToken = tokenManager.getValidToken(userId, PROVIDER, clientId, clientSecret);
        String url = "https://api.fitbit.com/1/user/-/activities/date/" + date + ".json";
        return getFitBitData(url, accessToken);
    }

    //Get sleep data:
    public JsonNode getSleep(Long userId, String clientId, String clientSecret, String date) {
        String accessToken = tokenManager.getValidToken(userId, PROVIDER, clientId, clientSecret);
        String url = "https://api.fitbit.com/1.2/user/-/sleep/date/" + date + ".json";
        return getFitBitData(url, accessToken);
    }

    //Get heart rate:
    public JsonNode getHeartRate(Long userId, String clientId, String clientSecret, String date) {
        String accessToken = tokenManager.getValidToken(userId, PROVIDER, clientId, clientSecret);
        String url = "https://api.fitbit.com/1/user/-/activities/heart/date/" + date + "/1d.json";
        return getFitBitData(url, accessToken);
    }
}
