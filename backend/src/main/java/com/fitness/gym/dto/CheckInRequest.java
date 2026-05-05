package com.fitness.gym.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CheckInRequest(@NotNull Long memberId, LocalDateTime checkinTime) {
}
