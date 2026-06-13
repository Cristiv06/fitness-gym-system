package com.fitness.userservice.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDateTime;

public record CreateMyGymClassRequest(
        @NotNull Long roomId,
        @NotBlank @Size(max = 120) String title,
        @NotNull LocalDateTime startTime,
        @NotNull LocalDateTime endTime,
        @NotNull @Min(1) Integer maxParticipants
) {}
