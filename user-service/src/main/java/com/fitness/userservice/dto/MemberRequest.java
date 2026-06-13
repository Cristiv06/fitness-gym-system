package com.fitness.userservice.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record MemberRequest(
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(max = 120) String fullName,
        @Size(max = 30) String phone,
        @Past LocalDate dateOfBirth
) {}
