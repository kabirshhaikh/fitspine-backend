package com.fitspine.service.impl;

import com.fitspine.dto.*;
import com.fitspine.enums.Role;
import com.fitspine.exception.UserAlreadyExistsException;
import com.fitspine.exception.UserNotFoundException;
import com.fitspine.helper.UserHelper;
import com.fitspine.model.User;
import com.fitspine.model.UserDiscIssue;
import com.fitspine.model.UserInjury;
import com.fitspine.model.UserSurgery;
import com.fitspine.repository.UserDiscIssueRepository;
import com.fitspine.repository.UserInjuryRepository;
import com.fitspine.repository.UserRepository;
import com.fitspine.repository.UserSurgeryRepository;
import com.fitspine.service.JwtService;
import com.fitspine.service.S3Service;
import com.fitspine.service.UserService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class UserServiceImp implements UserService {
    private final UserRepository userRepository;
    private final UserInjuryRepository userInjuryRepository;
    private final UserSurgeryRepository userSurgeryRepository;
    private final UserDiscIssueRepository userDiscIssueRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserHelper userHelper;
    private final S3Service s3Service;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public UserServiceImp(UserRepository userRepository, UserInjuryRepository userInjuryRepository, UserSurgeryRepository userSurgeryRepository, UserDiscIssueRepository userDiscIssueRepository, PasswordEncoder passwordEncoder, UserHelper userHelper, S3Service s3Service, JwtService jwtService, AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.userInjuryRepository = userInjuryRepository;
        this.userSurgeryRepository = userSurgeryRepository;
        this.userDiscIssueRepository = userDiscIssueRepository;
        this.passwordEncoder = passwordEncoder;
        this.userHelper = userHelper;
        this.s3Service = s3Service;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
    }


    @Override
    public LoginResponseDto loginUser(LoginRequestDto request) {
        var auth = new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword());
        authenticationManager.authenticate(auth);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + request.getEmail()));

        String token = jwtService.generateToken(user);

        String preSignedUrl = null;

        if (user.getProfilePicture() != null) {
            preSignedUrl = s3Service.generatePreSignedUrl(user.getProfilePicture());
        }

        return LoginResponseDto.builder()
                .profilePicture(preSignedUrl)
                .token(token)
                .email(user.getEmail())
                .fullName(user.getFullName())
                .id(user.getId())
                .build();
    }

    @Transactional
    @Override
    public UserResponseDto registerUser(UserRegisterDto dto) {
        //Check if email already exists:
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException("User already exists with email: " + dto.getEmail());
        }

        //Build and save user:
        User user = User.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .age(dto.getAge())
                .gender(dto.getGender())
                .surgeryHistory(dto.getSurgeryHistory())
                .isResearchOpt(dto.getIsResearchOpt())
                .isWearableConnected(false)
                .role(Role.USER)
                .build();

        User savedUser = userRepository.save(user);

        //Save profile picture:
        if (dto.getProfilePicture() != null && !dto.getProfilePicture().isEmpty()) {
            String fileName = userHelper.returnProfilePictureFileName(savedUser.getId(), dto.getProfilePicture().getOriginalFilename());
            String path = s3Service.uploadFile(dto.getProfilePicture(), fileName);
            savedUser.setProfilePicture(path);
            userRepository.save(savedUser);
        }

        List<UserInjury> userInjuryList = new ArrayList<>();
        if (dto.getUserInjuries() != null) {
            userInjuryList = userHelper.returnUserInjuryList(dto.getUserInjuries(), savedUser);
            userInjuryRepository.saveAll(userInjuryList);
        }

        List<UserSurgery> userSurgeryList = new ArrayList<>();
        if (dto.getUserSurgeries() != null) {
            userSurgeryList = userHelper.returnUserSurgeryList(dto.getUserSurgeries(), savedUser);
            userSurgeryRepository.saveAll(userSurgeryList);
        }

        List<UserDiscIssue> userDiscIssueList = new ArrayList<>();
        if (dto.getUserDiscIssues() != null) {
            userDiscIssueList = userHelper.returnUserDiscIssueList(dto.getUserDiscIssues(), savedUser);
            userDiscIssueRepository.saveAll(userDiscIssueList);
        }

        String preSignedProfilePictureUrl = null;
        if (savedUser.getProfilePicture() != null) {
            preSignedProfilePictureUrl = s3Service.generatePreSignedUrl(savedUser.getProfilePicture());
        }

        //Return response:
        return UserResponseDto.builder()
                .id(savedUser.getId())
                .fullName(savedUser.getFullName())
                .email(savedUser.getEmail())
                .age(savedUser.getAge())
                .gender(savedUser.getGender())
                .profilePicture(preSignedProfilePictureUrl)
                .isResearchOpt(savedUser.getIsResearchOpt())
                .isWearableConnected(savedUser.getIsWearableConnected())
                .wearableType(savedUser.getWearableType())
                .role(savedUser.getRole())
                .userInjuries(userHelper.returnMappedUserInjuryListDto(userInjuryList))
                .userSurgeries(userHelper.returnMappedUserSurgeryListDto(userSurgeryList))
                .userDiscIssues(userHelper.returnMappedUserDiscIssueDto(userDiscIssueList))
                .build();
    }

    @Transactional
    @Override
    public UserResponseDto updateUser(Long id, UserUpdateDto dto) {
        //Find existing User:
        User existingUser = userRepository.findById(id).orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));

        //First update the profile picture: (check if existing user and userRegisterDto has profilePicture)
        //If yes then delete the current profile picture from db and aws and then upload a new one and save it for user.
        if (dto.getProfilePicture() != null && !dto.getProfilePicture().isEmpty()) {
            if (existingUser.getProfilePicture() != null) {
                s3Service.deleteFile(existingUser.getProfilePicture());
            }

            String newPathForProfilePicture = userHelper.returnProfilePictureFileName(id, dto.getProfilePicture().getOriginalFilename());
            String s3Path = s3Service.uploadFile(dto.getProfilePicture(), newPathForProfilePicture);
            existingUser.setProfilePicture(s3Path);
            userRepository.save(existingUser);
        }

        //If the fields from dto are not null then replace it with existing values of user:
        if (dto.getAge() != null) {
            existingUser.setAge(dto.getAge());
        }

        if (dto.getGender() != null) {
            existingUser.setGender(dto.getGender());
        }

        if (dto.getSurgeryHistory() != null) {
            existingUser.setSurgeryHistory(dto.getSurgeryHistory());
        }

        if (dto.getIsResearchOpt() != null) {
            existingUser.setIsResearchOpt(dto.getIsResearchOpt());
        }

        if (dto.getIsWearableConnected() != null) {
            existingUser.setIsWearableConnected(dto.getIsWearableConnected());
        }

        if (dto.getWearableType() != null) {
            existingUser.setWearableType(dto.getWearableType());
        }

        userRepository.save(existingUser);

        //Update the list of User Surgeries, User Disc Issue and User Injury Type:
        List<UserInjury> userInjuries = new ArrayList<>();
        if (dto.getUserInjuries() != null) {
            userInjuryRepository.deleteAllByUserId(existingUser.getId());
            userInjuries = userHelper.returnUserInjuryList(dto.getUserInjuries(), existingUser);
            userInjuryRepository.saveAll(userInjuries);
        }

        List<UserSurgery> userSurgeries = new ArrayList<>();
        if (dto.getUserSurgeries() != null) {
            userSurgeryRepository.deleteAllByUserId(existingUser.getId());
            userSurgeries = userHelper.returnUserSurgeryList(dto.getUserSurgeries(), existingUser);
            userSurgeryRepository.saveAll(userSurgeries);
        }

        List<UserDiscIssue> userDiscIssues = new ArrayList<>();
        if (dto.getUserDiscIssues() != null) {
            userDiscIssueRepository.deleteAllByUserId(existingUser.getId());
            userDiscIssues = userHelper.returnUserDiscIssueList(dto.getUserDiscIssues(), existingUser);
            userDiscIssueRepository.saveAll(userDiscIssues);
        }

        //Get pre-signed url of profile picture if exists:
        String preSignedProfilePictureUrl = null;
        if (existingUser.getProfilePicture() != null) {
            preSignedProfilePictureUrl = s3Service.generatePreSignedUrl(existingUser.getProfilePicture());
        }

        //Return response:
        return UserResponseDto.builder()
                .id(existingUser.getId())
                .fullName(existingUser.getFullName())
                .email(existingUser.getEmail())
                .age(existingUser.getAge())
                .gender(existingUser.getGender())
                .profilePicture(preSignedProfilePictureUrl)
                .isResearchOpt(existingUser.getIsResearchOpt())
                .isWearableConnected(existingUser.getIsWearableConnected())
                .wearableType(existingUser.getWearableType())
                .userInjuries(userHelper.returnMappedUserInjuryListDto(userInjuries))
                .userSurgeries(userHelper.returnMappedUserSurgeryListDto(userSurgeries))
                .userDiscIssues(userHelper.returnMappedUserDiscIssueDto(userDiscIssues))
                .build();
    }
}
