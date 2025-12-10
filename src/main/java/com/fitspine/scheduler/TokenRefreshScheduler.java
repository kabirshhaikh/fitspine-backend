package com.fitspine.scheduler;

import com.fitspine.repository.UserWearableTokenRepository;
import com.fitspine.service.TokenManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

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

    @Scheduled(cron = "0 0 * * * ?") //runs every hour on the hour
    public void refreshExpiringTokens() {
        log.info("Running daily token refresh job...");
        AtomicInteger refreshedCount = new AtomicInteger(0);

        userWearableTokenRepository.findAll().forEach(token -> {
            try {
                if (token.getExpiresAt().isBefore(java.time.LocalDateTime.now().plusHours(1))) {
                    tokenManager.getValidToken(
                            token.getUser().getId(),
                            token.getProvider(),
                            clientId,
                            clientSecret
                    );
                    refreshedCount.incrementAndGet();
                }
            } catch (Exception e) {
                log.error("Failed to refresh token for user {}", token.getUser().getEmail(), e);
            }
        });

        log.info("Cronjob -> count of refreshed tokens {}", refreshedCount);
    }
}
