package com.fitspine.service;

import com.fitspine.exception.UserNotFoundException;
import com.fitspine.model.User;
import com.fitspine.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@Slf4j
public class EmailSenderService {
    @Value("${app.frontend.url}")
    private String frontEndBaseUrl;
    private final JavaMailSender mailSender;
    private final StringRedisTemplate redisTemplate;
    private final RedisTemplate<String, Object> redis;
    private final UserRepository userRepository;

    public EmailSenderService(JavaMailSender mailSender,
                              StringRedisTemplate redisTemplate,
                              RedisTemplate<String, Object> redis,
                              UserRepository userRepository
    ) {
        this.mailSender = mailSender;
        this.redisTemplate = redisTemplate;
        this.redis = redis;
        this.userRepository = userRepository;
    }

    public void sendPasswordResetEmail(String toEmail) {
        //Check for the email:
        User user = userRepository.findByEmail(toEmail)
                .orElseThrow(() -> new UserNotFoundException(
                        String.format("User with email '%s' does not exist. Cannot send reset link.", toEmail)
                ));

        String normalizedEmail = user.getEmail().trim().toLowerCase();
        String resetLink = frontEndBaseUrl + "/reset-password?email=" + normalizedEmail;
        String token = generateNumericToken();
        String subject = "FitSpine - Reset Your Password";
        String message = String.format(
                "Hi,\n\nClick the link below to reset your password:\n%s\n\n" +
                        "Once the page opens, enter this security code:\n\n" +
                        "%s\n\n" +
                        "This link and code will expire in 15 minutes.\n\n" +
                        "If you didn’t request this, please ignore this email.\n\n" +
                        "— FitSpine Team",
                resetLink,
                token
        );

        String key = "password_reset_email:" + normalizedEmail;
        redis.opsForValue().set(key, token, Duration.ofMinutes(15));
        log.info("Stored password reset token for {} (expires in 15 minutes)", normalizedEmail);

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(toEmail);
        email.setSubject(subject);
        email.setText(message);
        email.setFrom("no-reply@fit-spine.app");

        mailSender.send(email);
    }

    public String generateNumericToken() {
        SecureRandom random = new SecureRandom();
        int number = 100000 + random.nextInt(900000);
        return String.valueOf(number);
    }
}
