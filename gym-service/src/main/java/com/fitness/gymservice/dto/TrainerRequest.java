package com.fitness.gymservice.dto;

import jakarta.validation.constraints.*;

public record TrainerRequest(
        @NotBlank @Size(max = 120) String fullName,
        @Size(max = 120) String specialization,
        @Size(max = 30) String phone,
        @Email @Size(max = 150) String email
) {}
