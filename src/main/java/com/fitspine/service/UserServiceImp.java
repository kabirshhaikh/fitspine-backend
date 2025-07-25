package com.fitspine.service;

import com.fitspine.dto.UserRegisterDto;
import com.fitspine.dto.UserResponseDto;
import com.fitspine.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImp implements UserService {
    private final UserRepository userRepository;

    public UserServiceImp(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserResponseDto registerUser(UserRegisterDto userRegisterDto) {

    }

}
