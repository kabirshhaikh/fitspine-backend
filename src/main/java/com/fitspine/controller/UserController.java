package com.fitspine.controller;

import com.fitspine.dto.UserRegisterDto;
import com.fitspine.dto.UserResponseDto;
import com.fitspine.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
