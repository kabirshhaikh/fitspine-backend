package com.fitspine.service;

import com.fitspine.dto.ForgotPasswordResponseDto;
import com.fitspine.exception.PasswordMismatchException;
import com.fitspine.exception.PasswordReuseException;
import com.fitspine.exception.TokenExpiredException;
import com.fitspine.exception.UserNotFoundException;
import com.fitspine.model.User;
import com.fitspine.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
@Slf4j
public class EmailSenderService {
    @Value("${app.frontend.url}")
    private String frontEndBaseUrl;
    private final RedisTemplate<String, String> redis;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final GraphEmailSender graphEmailSender;

    public EmailSenderService(
            GraphEmailSender graphEmailSender,
            RedisTemplate<String, String> redis,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.graphEmailSender = graphEmailSender;
        this.redis = redis;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
        String subject = "Sphinic - Reset Your Password";
        String message = String.format(
                "Hi,\n\nClick the link below to reset your password:\n%s\n\n" +
                        "Once the page opens, enter this security code:\n\n" +
                        "%s\n\n" +
                        "This link and code will expire in 15 minutes.\n\n" +
                        "If you didn’t request this, please ignore this email.\n\n" +
                        "— Sphinic Team",
                resetLink,
                token
        );

        String key = "password_reset_email:" + user.getPublicId();
        redis.opsForValue().set(key, token, Duration.ofMinutes(15));
        log.info("Stored password reset token for {} (expires in 15 minutes)", normalizedEmail);

        graphEmailSender.sendPlainTextEmail(toEmail, subject, message);
    }

    public void resetPassword(ForgotPasswordResponseDto dto) {
        User user = userRepository.findByEmail(dto.getEmail()).orElseThrow(() -> new UserNotFoundException("User with email: " + dto.getEmail() + " not found to reset password"));

        String key = "password_reset_email:" + user.getPublicId();
        String token = redis.opsForValue().get(key);

        //Check if the token is expired or if the token is null.
        if (token == null) {
            throw new TokenExpiredException("Your password reset code has expired. Please request a new one");
        }

        //Get the old password and check if new password is same as old password?
        if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
            throw new PasswordReuseException("New password cannot be same as the old password");
        }

        //Compare the new password and confirm password:

        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new PasswordMismatchException("New password and confirm password do not match");
        }

        //Update the new password for the user:
        String newEncodedPassword = passwordEncoder.encode(dto.getNewPassword());
        user.setPassword(newEncodedPassword);
        userRepository.save(user);

        //Delete key from redis:
        redis.delete(key);
    }

    public String generateNumericToken() {
        SecureRandom random = new SecureRandom();
        int number = 100000 + random.nextInt(900000);
        return String.valueOf(number);
    }
}
