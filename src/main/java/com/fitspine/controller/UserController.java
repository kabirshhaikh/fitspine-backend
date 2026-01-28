package com.fitspine.controller;

import com.fitspine.dto.*;
import com.fitspine.service.EmailSenderService;
import com.fitspine.service.GoogleTokenVerificationService;
import com.fitspine.service.JwtService;
import com.fitspine.service.UserService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;
    private final EmailSenderService emailSenderService;
    private final GoogleTokenVerificationService googleTokenVerificationService;


    public UserController(UserService userService, EmailSenderService emailSenderService, GoogleTokenVerificationService googleTokenVerificationService) {
        this.userService = userService;
        this.emailSenderService = emailSenderService;
        this.googleTokenVerificationService = googleTokenVerificationService;
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LoginResponseDto> registerUser(@ModelAttribute @Valid UserRegisterDto userRegisterDto) {
        LoginResponseDto userResponse = userService.registerUser(userRegisterDto);
        return ResponseEntity.ok(userResponse);
    }

    @PostMapping(value = "/login")
    public ResponseEntity<LoginResponseDto> loginUser(@RequestBody @Valid LoginRequestDto request) {
        LoginResponseDto response = userService.loginUser(request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponseDto> updateUser(@ModelAttribute UserUpdateDto userUpdateDto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        UserResponseDto userResponseDto = userService.updateUser(userUpdateDto, email);
        return ResponseEntity.ok(userResponseDto);
    }

    @GetMapping(value = "/me")
    public ResponseEntity<UserProfileDto> userProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        UserProfileDto userProfile = userService.userProfile(email);
        return ResponseEntity.ok(userProfile);
    }

    @PostMapping(value = "/forgot-password")
    public ResponseEntity<String> sendEmail(@RequestBody @Valid ForgotPasswordDto request) {
        emailSenderService.sendPasswordResetEmail(request.getEmail());
        return ResponseEntity.ok("Password reset link and security code sent to your email.");
    }

    @PostMapping(value = "/set-password")
    public ResponseEntity<String> resetPassword(@RequestBody @Valid ForgotPasswordResponseDto dto) {
        emailSenderService.resetPassword(dto);
        return ResponseEntity.ok("Password has been reset successfully");
    }

    @PatchMapping(value = "/onboarding-complete")
    public ResponseEntity<Void> completeOnboarding() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        userService.markOnboardingCompleted(email);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/auth/google/register")
    public ResponseEntity<?> registerWithGoogle(@RequestBody GoogleRegisterRequestDto dto) {
        GoogleIdToken.Payload payload = googleTokenVerificationService.verifyAndGetPayload(dto.getIdToken());
        String email = payload.getEmail();
        String providerId = payload.getSubject();
        String fullName = payload.get("name").toString();

        return ResponseEntity.ok(
                userService.registerOrLoginGoogleUser(email, providerId, fullName)
        );
    }

    @PostMapping(value = "/auth/google/register-partial-user", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<LoginResponseDto> registerPartialUser(@ModelAttribute @Valid RegisterPartialUserDto dto) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        LoginResponseDto userResponse = userService.registerPartialUser(dto, email);
        return ResponseEntity.ok(userResponse);
    }


}
