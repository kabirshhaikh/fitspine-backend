package com.fitspine.controller;

import com.fitspine.dto.*;
import com.fitspine.service.JwtService;
import com.fitspine.service.UserService;
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


    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponseDto> registerUser(@ModelAttribute @Valid UserRegisterDto userRegisterDto) {
        UserResponseDto userResponse = userService.registerUser(userRegisterDto);
        return ResponseEntity.ok(userResponse);
    }

    @PostMapping(value = "/login")
    public ResponseEntity<LoginResponseDto> loginUser(@RequestBody @Valid LoginRequestDto request) {
        LoginResponseDto response = userService.loginUser(request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponseDto> updateUser(@PathVariable Long id, @ModelAttribute UserUpdateDto userUpdateDto) {
        UserResponseDto userResponseDto = userService.updateUser(id, userUpdateDto);
        return ResponseEntity.ok(userResponseDto);
    }

    @GetMapping(value = "/me")
    public ResponseEntity<UserProfileDto> userProfile() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        UserProfileDto userProfile = userService.userProfile(email);
        return ResponseEntity.ok(userProfile);
    }
}
