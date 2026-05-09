package com.fitness.gym.dto;

import java.time.LocalDateTime;

public record GymClassResponse(
        Long classId,
        Long trainerId,
        String trainerName,
        Long roomId,
        String title,
        LocalDateTime startTime,
        LocalDateTime endTime,
        int maxParticipants) {
}
