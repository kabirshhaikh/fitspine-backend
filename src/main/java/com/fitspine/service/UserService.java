package com.fitspine.service;

import com.fitspine.dto.UserRegisterDto;
import com.fitspine.dto.UserResponseDto;

public interface UserService {
    UserResponseDto registerUser(UserRegisterDto userRegisterDto);
}
