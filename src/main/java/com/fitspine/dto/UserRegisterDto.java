package com.fitspine.dto;

import com.fitspine.enums.Gender;
import com.fitspine.enums.WearableType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class UserRegisterDto {

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    private String email;

    @NotBlank(message = "Password is required")
    private String password;

    @NotNull(message = "Age is required")
    @Min(value = 18, message = "Age must be 18 or greater")
    private Integer age;

    @NotNull(message = "Gender is required")
    private Gender gender;

    private MultipartFile profilePicture;

    @NotNull(message = "Surgery history is required")
    private Boolean surgeryHistory;

    @NotNull(message = "Research Opt is required")
    private Boolean isResearchOpt;

    @NotNull(message = "You must accept the Terms and Privacy Policy")
    private Boolean acceptedTerms;

    private List<UserInjuryDto> userInjuries;

    private List<UserSurgeryDto> userSurgeries;

    private List<UserDiscIssueDto> userDiscIssues;
}
