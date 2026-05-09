package com.fitness.gym.dto;

import jakarta.validation.constraints.NotNull;

public record EnrollMyClassRequest(@NotNull Long classId) {
}
