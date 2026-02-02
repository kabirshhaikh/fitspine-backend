package com.fitspine.scheduler;

import com.fitspine.repository.UserRepository;
import com.fitspine.service.DailyReminderEmailService;
import com.fitspine.service.UserIdEmailProjection;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@Slf4j
public class DailyReminderEmail {
    private final UserRepository userRepository;
    private final DailyReminderEmailService emailService;

    public DailyReminderEmail(
            UserRepository userRepository,
            DailyReminderEmailService emailService
    ) {
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @Scheduled(cron = "0 0 20 * * ?") // 8 PM daily
    public void sendDailyReminderEmails() {
        int emailsSent = 0;
        LocalDate today = LocalDate.now();
        List<UserIdEmailProjection> users = userRepository.findUsersWithoutManualLog(today);

        for (int i = 0; i < users.size(); i++) {
            String email = users.get(i).getEmail();
            String fullName = users.get(i).getFullName();

            if (email == null) {
                log.warn(
                        "EMAIL CRON -> Skipping user with null email on {}",
                        today
                );
                continue;
            } else {
                log.info("Sending daily reminder email to user:: {} on date: {}", fullName, today);
                emailService.sendDailyReminderEmail(fullName, email);
                emailsSent++;
            }
        }

        log.info("Daily reminder emails send: {}", emailsSent);
    }
}
