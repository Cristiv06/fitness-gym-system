package com.fitness.gym.dto;

import jakarta.validation.constraints.NotNull;

public record ClassEnrollmentRequest(@NotNull Long memberId, @NotNull Long classId) {
}
