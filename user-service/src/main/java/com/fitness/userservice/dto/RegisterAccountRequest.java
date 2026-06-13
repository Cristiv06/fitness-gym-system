package com.fitness.userservice.dto;

import jakarta.validation.constraints.*;
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
) {}
