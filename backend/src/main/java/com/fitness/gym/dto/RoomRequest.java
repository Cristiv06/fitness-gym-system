package com.fitness.gym.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record RoomRequest(
        @NotBlank @Size(max = 80) String name,
        @NotNull @Min(1) Integer maxCapacity,
        List<Long> equipmentIds) {
}
