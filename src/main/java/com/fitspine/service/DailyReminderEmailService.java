package com.fitspine.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
@Slf4j
public class DailyReminderEmailService {
    @Value("${app.frontend.url}")
    private String frontEndBaseUrl;
    private final GraphEmailSender graphEmailSender;

    public DailyReminderEmailService(GraphEmailSender graphEmailSender) {
        this.graphEmailSender = graphEmailSender;
    }

    public void sendDailyReminderEmail(String fullName, String email) {
        try {
            String body = this.buildEmailBody(fullName);
            String subject = "Quick reminder to log today’s activity";

            log.info(
                    "EMAIL -> Sending daily reminder email to {}",
                    email
            );

            graphEmailSender.sendPlainTextEmail(
                    email,
                    subject,
                    body
            );

            log.info(
                    "EMAIL -> Successfully sent reminder to {}",
                    email
            );

            Thread.sleep(300);
        } catch (Exception ex) {
            log.error(
                    "EMAIL FAILED -> email={}, date={}, error={}",
                    email,
                    ex.getMessage(),
                    ex
            );
        }
    }

    private String buildEmailBody(String fullName) {
        String link = frontEndBaseUrl;

        String greetingName =
                (fullName == null || fullName.isBlank())
                        ? "there"
                        : fullName.split(" ")[0]; // first name only

        return """
                Hey %s 👋

                You haven’t logged your manual spine data for today.

                Taking 30 seconds to log your day helps:
                • keep insights accurate
                • spot pain patterns early
                • improve long-term outcomes

                👉 Open the app: %s

                — Sphinic - AI-Powered Spine Health Tracking
                """.formatted(greetingName, link);
    }
}
