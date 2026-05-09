package com.fitness.gym.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AdminCreateAccountRequest(
        @NotBlank @Size(min = 3, max = 50) String username,
        @NotBlank @Size(min = 8, max = 100) String password,
        @NotBlank @Email @Size(max = 150) String email,
        @NotBlank @Size(max = 120) String fullName,
        @Size(max = 30) String phone,
        @Size(max = 120) String specialization
) {
}
