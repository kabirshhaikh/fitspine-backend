package com.fitspine.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fitspine.enums.WearableType;
import com.fitspine.exception.TokenRefreshException;
import com.fitspine.exception.UserNotFoundException;
import com.fitspine.model.User;
import com.fitspine.model.UserWearableToken;
import com.fitspine.repository.UserRepository;
import com.fitspine.repository.UserWearableTokenRepository;
import com.fitspine.service.WearableService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
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

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper;

    public FitbitServiceImpl(UserWearableTokenRepository userWearableTokenRepository, UserRepository userRepository, RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.userWearableTokenRepository = userWearableTokenRepository;
        this.userRepository = userRepository;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
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
    public void exchangeCodeForToken(String code, Long userId) {
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
            User user = userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException("User not found with id:" + userId));
            JsonNode jsonNode = objectMapper.readTree(response.getBody());

            if (jsonNode.has("errors")) {
                throw new TokenRefreshException("Fitbit returned an error: " + jsonNode.get("errors").toString());
            }

            String accessToken = jsonNode.get("access_token").asText();
            String refreshToken = jsonNode.get("refresh_token").asText();
            int expiresIn = jsonNode.get("expires_in").asInt();
            String tokenType = jsonNode.get("token_type").asText();
            String scope = jsonNode.get("scope").asText();

            userWearableTokenRepository.deleteByUserIdAndProvider(userId, getProvider());

            UserWearableToken token = new UserWearableToken();
            token.setUser(user);
            token.setAccessToken(accessToken);
            token.setRefreshToken(refreshToken);
            token.setProvider(getProvider());
            token.setTokenType(tokenType);
            token.setScope(scope);
            token.setExpiresAt(LocalDateTime.now().plusSeconds(expiresIn));
            userWearableTokenRepository.save(token);

            //Update user wearable info:
            user.setIsWearableConnected(true);
            user.setWearableType(WearableType.FITBIT);
            userRepository.save(user);
        } catch (Exception e) {
            throw new TokenRefreshException("Error parsing fitbit token response", e);
        }
    }

    @Override
    public String getProvider() {
        return "FITBIT";
    }

    @Transactional
    @Override
    public void revoke(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));

        Optional<UserWearableToken> tokenOptional = userWearableTokenRepository.findByUserIdAndProvider(user.getId(), getProvider());

        if (tokenOptional.isEmpty()) {
            user.setIsWearableConnected(false);
            user.setWearableType(null);
            userRepository.save(user);
            return;
        }

        UserWearableToken token = tokenOptional.get();

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setBearerAuth(token.getAccessToken());

            MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
            body.add("token", token.getAccessToken());

            HttpEntity<MultiValueMap<String, String>> request =
                    new HttpEntity<>(body, headers);

            restTemplate.postForEntity(
                    "https://api.fitbit.com/oauth2/revoke",
                    request,
                    Void.class
            );
        } catch (Exception exception) {
            log.warn("Fitbit revoke call failed, doing local cleanup", exception);
        }

        //Clean up locally:
        userWearableTokenRepository.deleteByUserIdAndProvider(user.getId(), getProvider());
        user.setIsWearableConnected(false);
        user.setWearableType(null);
        userRepository.save(user);
    }
}
