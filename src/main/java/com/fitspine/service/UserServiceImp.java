package com.fitspine.service;

import com.fitspine.dto.UserRegisterDto;
import com.fitspine.dto.UserResponseDto;
import com.fitspine.helper.UserHelper;
import com.fitspine.model.User;
import com.fitspine.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImp implements UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserHelper userHelper;

    public UserServiceImp(UserRepository userRepository, PasswordEncoder passwordEncoder, UserHelper userHelper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userHelper = userHelper;
    }

    @Override
    public UserResponseDto registerUser(UserRegisterDto userRegisterDto) {
        User user = new User();

        return UserResponseDto.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .age(user.getAge())
                .gender(user.getGender())
                .isResearchOpt(user.getIsResearchOpt())
                .isWearableConnected(user.getIsWearableConnected())
                .wearableType(user.getWearableType())
                .build();
    }

}
