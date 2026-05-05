package com.fitness.gym.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EquipmentRequest(@NotBlank @Size(max = 80) String name) {
}
