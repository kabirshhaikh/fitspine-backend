package com.fitspine.service.impl;

import com.fitspine.dto.UserRegisterDto;
import com.fitspine.dto.UserResponseDto;
import com.fitspine.exception.UserAlreadyExistsException;
import com.fitspine.helper.UserHelper;
import com.fitspine.model.User;
import com.fitspine.model.UserDiscIssue;
import com.fitspine.model.UserInjury;
import com.fitspine.model.UserSurgery;
import com.fitspine.repository.UserDiscIssueRepository;
import com.fitspine.repository.UserInjuryRepository;
import com.fitspine.repository.UserRepository;
import com.fitspine.repository.UserSurgeryRepository;
import com.fitspine.service.S3Service;
import com.fitspine.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    public UserServiceImp(UserRepository userRepository, UserInjuryRepository userInjuryRepository, UserSurgeryRepository userSurgeryRepository, UserDiscIssueRepository userDiscIssueRepository, PasswordEncoder passwordEncoder, UserHelper userHelper, S3Service s3Service) {
        this.userRepository = userRepository;
        this.userInjuryRepository = userInjuryRepository;
        this.userSurgeryRepository = userSurgeryRepository;
        this.userDiscIssueRepository = userDiscIssueRepository;
        this.passwordEncoder = passwordEncoder;
        this.userHelper = userHelper;
        this.s3Service = s3Service;
    }

    @Override
    public UserResponseDto registerUser(UserRegisterDto dto) {
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new UserAlreadyExistsException("User already exists with email: " + dto.getEmail());
        }

        User user = User.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .age(dto.getAge())
                .gender(dto.getGender())
                .surgeryHistory(false)
                .isResearchOpt(true)
                .isWearableConnected(false)
                .build();

        User savedUser = userRepository.save(user);

        if (dto.getProfilePicture() != null && !dto.getProfilePicture().isEmpty()) {
            String fileName = userHelper.returnProfilePictureFileName(savedUser.getId(), dto);
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
                .userInjuries(userHelper.returnMappedUserInjuryListDto(userInjuryList))
                .userSurgeries(userHelper.returnMappedUserSurgeryListDto(userSurgeryList))
                .userDiscIssues(userHelper.returnMappedUserDiscIssueDto(userDiscIssueList))
                .build();
    }
}
