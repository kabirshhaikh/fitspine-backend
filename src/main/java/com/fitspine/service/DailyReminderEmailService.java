package com.fitspine.service;

import com.fitspine.exception.TokenExpiredException;
import com.fitspine.exception.UserNotFoundException;
import com.fitspine.model.User;
import com.fitspine.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.UUID;


@Service
@Slf4j
public class DailyReminderEmailService {
    @Value("${app.frontend.url}")
    private String frontEndBaseUrl;
    private final GraphEmailSender graphEmailSender;

    private final RedisTemplate<String, String> redis;

    private final UserRepository userRepository;

    public DailyReminderEmailService(
            GraphEmailSender graphEmailSender,
            RedisTemplate<String, String> redis,
            UserRepository userRepository
    ) {
        this.graphEmailSender = graphEmailSender;
        this.redis = redis;
        this.userRepository = userRepository;
    }

    public void sendDailyReminderEmail(String fullName, String email, String publicId) {
        try {
            //set token
            String token = UUID.randomUUID().toString();

            //set key
            String key = "unsubscribe_email:" + token;

            //add key to redis
            redis.opsForValue().set(key, publicId, Duration.ofMinutes(15));
            log.info("Unsubscribe key value set for user: {}", publicId);

            //create link to unsubscribe
            String unsubscribeLink = frontEndBaseUrl + "/unsubscribe?token=" + token;

            String body = this.buildEmailBody(fullName, unsubscribeLink);
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

    public void unsubscribeUser(String token) {
        String redisKey = "unsubscribe_email:" + token;

        String publicId = redis.opsForValue().get(redisKey);

        if (publicId == null) {
            throw new TokenExpiredException("Unsubscribe token has expired or is invalid");
        }

        User user = userRepository.findByPublicId(publicId).orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setEmailRemindersEnabled(false);
        userRepository.save(user);

        redis.delete(redisKey);
        log.info("User:{} unsubscribed from emails", publicId);
    }

    private String buildEmailBody(String fullName, String unsubscribeLink) {
        String link = frontEndBaseUrl;

        String greetingName =
                (fullName == null || fullName.isBlank())
                        ? "there"
                        : fullName.split(" ")[0]; // first name only

        return """
                Hey %s 👋

                Just a quick reminder — you haven’t logged your manual spine data today.

                Logging your day takes less than a minute and helps you:
                            
                  • keep your insights accurate
                  • catch pain or flare-up patterns early
                  • make smarter long-term spine decisions


                👉 Open the app:
                %s


                If you no longer want to receive these reminder emails,
                you can unsubscribe here:
                %s


                ——————————————————
                Sphinic
                AI-Powered Spine Health Tracking
                """.formatted(greetingName, link, unsubscribeLink);
    }
}
