package com.fitness.gym.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MemberProfileRequest(
        @NotNull Long memberId,
        @Size(max = 120) String emergencyContact,
        @Size(max = 500) String notes) {
}
