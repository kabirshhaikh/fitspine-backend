package com.fitspine.dto;

import com.fitspine.enums.Gender;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterPartialUserDto {
    @NotNull(message = "Age is required")
    @Min(value = 0, message = "Age must be non-negative")
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
