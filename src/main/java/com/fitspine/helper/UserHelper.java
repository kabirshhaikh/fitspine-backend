package com.fitspine.helper;

import com.fitspine.dto.UserDiscIssueDto;
import com.fitspine.dto.UserInjuryDto;
import com.fitspine.dto.UserRegisterDto;
import com.fitspine.dto.UserSurgeryDto;
import com.fitspine.model.User;
import com.fitspine.model.UserDiscIssue;
import com.fitspine.model.UserInjury;
import com.fitspine.model.UserSurgery;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Component
public class UserHelper {
    public List<UserInjury> returnUserInjuryList(List<UserInjuryDto> dto, User savedUser) {
        List<UserInjury> list = new ArrayList<>();

        for (int i = 0; i < dto.size(); i++) {
            UserInjury userInjury = UserInjury.builder()
                    .user(savedUser)
                    .injuryType(dto.get(i).getInjuryType())
                    .build();

            list.add(userInjury);
        }

        return list;
    }

    public List<UserSurgery> returnUserSurgeryList(List<UserSurgeryDto> dto, User savedUser) {
        List<UserSurgery> list = new ArrayList<>();

        for (int i = 0; i < dto.size(); i++) {
            UserSurgery userSurgery = UserSurgery.builder()
                    .user(savedUser)
                    .surgeryType(dto.get(i).getSurgeryType())
                    .build();

            list.add(userSurgery);
        }

        return list;
    }

    public List<UserDiscIssue> returnUserDiscIssueList(List<UserDiscIssueDto> dto, User savedUser) {
        List<UserDiscIssue> list = new ArrayList<>();

        for (int i = 0; i < dto.size(); i++) {
            UserDiscIssue userDiscIssue = UserDiscIssue.builder()
                    .user(savedUser)
                    .discLevel(dto.get(i).getDiscLevel())
                    .build();

            list.add(userDiscIssue);
        }

        return list;
    }

    public List<UserInjuryDto> returnMappedUserInjuryListDto(List<UserInjury> userInjuries) {
        List<UserInjuryDto> list = new ArrayList<>();

        for (int i = 0; i < userInjuries.size(); i++) {
            UserInjuryDto userInjuryDto = new UserInjuryDto();
            userInjuryDto.setInjuryType(userInjuries.get(i).getInjuryType());

            list.add(userInjuryDto);
        }

        return list;
    }

    public List<UserSurgeryDto> returnMappedUserSurgeryListDto(List<UserSurgery> userSurgeries) {
        List<UserSurgeryDto> list = new ArrayList<>();

        for (int i = 0; i < userSurgeries.size(); i++) {
            UserSurgeryDto userSurgeryDto = new UserSurgeryDto();
            userSurgeryDto.setSurgeryType(userSurgeries.get(i).getSurgeryType());

            list.add(userSurgeryDto);
        }

        return list;
    }

    public List<UserDiscIssueDto> returnMappedUserDiscIssueDto(List<UserDiscIssue> userDiscIssues) {
        List<UserDiscIssueDto> list = new ArrayList<>();

        for (int i = 0; i < userDiscIssues.size(); i++) {
            UserDiscIssueDto userDiscIssueDto = new UserDiscIssueDto();
            userDiscIssueDto.setDiscLevel(userDiscIssues.get(i).getDiscLevel());

            list.add(userDiscIssueDto);
        }

        return list;
    }

    public String returnProfilePictureFileName(Long id, String fileName) {
        return "profile-pictures/" + id + "/" + fileName;
    }
}
