package com.fitspine.service;

import com.fitspine.dto.UserRegisterDto;
import com.fitspine.dto.UserResponseDto;
import com.fitspine.dto.UserUpdateDto;

public interface UserService {
    UserResponseDto registerUser(UserRegisterDto userRegisterDto);
    UserResponseDto updateUser(Long id, UserUpdateDto userUpdateDto);
}
