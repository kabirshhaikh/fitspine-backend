package com.fitspine.service;

import com.fitspine.dto.*;

public interface UserService {
    UserResponseDto registerUser(UserRegisterDto userRegisterDto);

    UserResponseDto updateUser(Long id, UserUpdateDto userUpdateDto);

    LoginResponseDto loginUser(LoginRequestDto request);
}
