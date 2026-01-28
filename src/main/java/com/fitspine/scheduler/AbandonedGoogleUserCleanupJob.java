package com.fitspine.scheduler;

import com.fitspine.enums.AuthProvider;
import com.fitspine.enums.Gender;
import com.fitspine.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
public class AbandonedGoogleUserCleanupJob {
    private final UserRepository userRepository;

    public AbandonedGoogleUserCleanupJob(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    @Scheduled(cron = "0 */10 * * * *") // every 10 minutes
    public void cleanup() {

        int deleted = userRepository.deleteAbandonedGoogleUsersNative(
                AuthProvider.GOOGLE.name(),
                Gender.OTHER.name()
        );

        log.info(
                "Abandoned Google user cleanup ran. Deleted {} users.",
                deleted
        );
    }
}
