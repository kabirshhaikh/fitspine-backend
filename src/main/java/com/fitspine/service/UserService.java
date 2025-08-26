package com.fitspine.service;

import com.fitspine.dto.*;

public interface UserService {
    UserResponseDto registerUser(UserRegisterDto userRegisterDto);

    UserResponseDto updateUser(UserUpdateDto userUpdateDto, String email);

    LoginResponseDto loginUser(LoginRequestDto request);

    UserProfileDto userProfile(String email);
}
