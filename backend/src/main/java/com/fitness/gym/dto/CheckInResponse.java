package com.fitness.gym.dto;

import java.time.LocalDateTime;

public record CheckInResponse(Long checkinId, Long memberId, LocalDateTime checkinTime) {
}
