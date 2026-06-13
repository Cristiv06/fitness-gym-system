package com.fitness.userservice.dto;

import jakarta.validation.constraints.*;

public record MemberProfileRequest(
        @NotNull Long memberId,
        @Size(max = 120) String emergencyContact,
        @Size(max = 500) String notes
) {}
