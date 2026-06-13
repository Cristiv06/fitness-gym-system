package com.fitness.gymservice.dto;

import jakarta.validation.constraints.*;

public record EquipmentRequest(@NotBlank @Size(max = 80) String name) {}
