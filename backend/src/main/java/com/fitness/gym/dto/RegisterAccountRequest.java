package com.fitness.gym.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record RegisterAccountRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotNull AccountType accountType,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(max = 120) String fullName,
        @Size(max = 30) String phone,
        @Past LocalDate dateOfBirth,
        @Size(max = 120) String specialization
) {
}
