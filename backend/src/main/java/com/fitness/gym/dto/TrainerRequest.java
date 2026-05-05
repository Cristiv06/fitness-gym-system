package com.fitness.gym.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record TrainerRequest(
        @NotBlank @Size(max = 120) String fullName,
        @Size(max = 120) String specialization,
        @Size(max = 30) String phone,
        @Size(max = 150) String email) {
}
