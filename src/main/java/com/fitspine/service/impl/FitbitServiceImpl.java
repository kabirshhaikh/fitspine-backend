package com.fitspine.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitspine.exception.UserNotFoundException;
import com.fitspine.model.User;
import com.fitspine.model.UserWearableToken;
import com.fitspine.repository.UserRepository;
import com.fitspine.repository.UserWearableTokenRepository;
import com.fitspine.service.WearableService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;

@Service
public class FitbitServiceImpl implements WearableService {
    @Value("${FITBIT_CLIENT_ID}")
    private String clientId;

    @Value("${FITBIT_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${FITBIT_REDIRECT_URL}")
    private String redirectUri;

    @Value("${FITBIT_AUTHORIZATION_URI}")
    private String fitBitAuthUrl;

    @Value("${FITBIT_ACCESS_REFRESH_URI}")
    private String tokenUri;

    private final UserWearableTokenRepository userWearableTokenRepository;

    private final UserRepository userRepository;

    private static final String SCOPE = "activity sleep heartrate weight respiratory_rate";

    private final RestTemplate restTemplate = new RestTemplate();

    private final ObjectMapper objectMapper = new ObjectMapper();

    public FitbitServiceImpl(UserWearableTokenRepository userWearableTokenRepository, UserRepository userRepository) {
        this.userWearableTokenRepository = userWearableTokenRepository;
        this.userRepository = userRepository;
    }


    @Override
    public String buildAuthUrl(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with email:" + email));
        Long userId = user.getId();
        return UriComponentsBuilder.fromHttpUrl(fitBitAuthUrl)
                .queryParam("client_id", clientId)
                .queryParam("response_type", "code")
                .queryParam("scope", SCOPE)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", userId)
                .build().toUriString();
    }

    @Override
    public void exchangeCodeForToken(String code, String email) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBasicAuth(clientId, clientSecret);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        String body = UriComponentsBuilder.newInstance()
                .queryParam("client_id", clientId)
                .queryParam("grant_type", "authorization_code")
                .queryParam("redirect_uri", redirectUri)
                .queryParam("code", code)
                .build().getQuery();

        HttpEntity<String> request = new HttpEntity<>(body, headers);

        ResponseEntity<String> response = restTemplate.postForEntity(tokenUri, request, String.class);

        try {
            User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("User not found with email:" + email));
            JsonNode jsonNode = objectMapper.readTree(response.getBody());
            String accessToken = jsonNode.get("access_token").asText();
            String refreshToken = jsonNode.get("refresh_token").asText();
            int expiresIn = jsonNode.get("expires_in").asInt();

            UserWearableToken token = new UserWearableToken();
            token.setUser(user);
            token.setAccessToken(accessToken);
            token.setRefreshToken(refreshToken);
            token.setExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));
            userWearableTokenRepository.save(token);
        } catch (Exception e) {
            throw new RuntimeException("Error parsing fitbit token response", e);
        }
    }

    @Override
    public String getProvider() {
        return "FITBIT";
    }
}
