package com.fitness.userservice.dto;

import jakarta.validation.constraints.NotNull;

public record EnrollMyClassRequest(@NotNull Long classId) {}
