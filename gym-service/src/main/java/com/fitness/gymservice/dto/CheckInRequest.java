package com.fitness.gymservice.dto;

import jakarta.validation.constraints.NotNull;

public record CheckInRequest(@NotNull Long memberId) {}
