package com.fitness.gymservice.dto;

import jakarta.validation.constraints.NotNull;

public record ClassEnrollmentRequest(@NotNull Long memberId, @NotNull Long classId) {}
