package com.fitness.gymservice.dto;

import jakarta.validation.constraints.*;
import java.util.Set;

public record RoomRequest(
        @NotBlank @Size(max = 80) String name,
        @NotNull @Min(1) Integer maxCapacity,
        Set<Long> equipmentIds
) {}
