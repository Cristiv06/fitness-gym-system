package com.fitness.gym.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record MemberRequest(
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(max = 120) String fullName,
        @Size(max = 30) String phone,
        @Past LocalDate dateOfBirth
) {
}
