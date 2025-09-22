package com.fitspine.scheduler;

import com.fitspine.repository.UserWearableTokenRepository;
import com.fitspine.service.TokenManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TokenRefreshScheduler {
    @Value("${FITBIT_CLIENT_SECRET}")
    private String clientSecret;

    @Value("${FITBIT_CLIENT_ID}")
    private String clientId;

    private final UserWearableTokenRepository userWearableTokenRepository;
    private final TokenManager tokenManager;

    public TokenRefreshScheduler(UserWearableTokenRepository userWearableTokenRepository, TokenManager tokenManager) {
        this.userWearableTokenRepository = userWearableTokenRepository;
        this.tokenManager = tokenManager;
    }

    @Scheduled(cron = "0 0 2 * * ?") // runs daily 2AM
    public void refreshExpiringTokens() {
        log.info("Running daily token refresh job...");

        userWearableTokenRepository.findAll().forEach(token -> {
            try {
                if (token.getExpiresAt().isBefore(java.time.LocalDateTime.now().plusHours(1))) {
                    tokenManager.getValidToken(
                            token.getUser().getId(),
                            token.getProvider(),
                            clientId,
                            clientSecret
                    );
                }
            } catch (Exception e) {
                log.error("Failed to refresh token for user {}", token.getUser().getEmail(), e);
            }
        });
    }
}
