package com.fitspine.controller;

import com.fitspine.dto.UserRegisterDto;
import com.fitspine.dto.UserResponseDto;
import com.fitspine.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> registerUser(@Valid @RequestBody UserRegisterDto userRegisterDto) {
        UserResponseDto userResponse = userService.registerUser(userRegisterDto);
        return ResponseEntity.ok(userResponse);
    }
}
