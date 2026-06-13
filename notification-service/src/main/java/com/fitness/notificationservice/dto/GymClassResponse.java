package com.fitness.notificationservice.dto;

import java.time.LocalDateTime;

public record GymClassResponse(Long classId, Long trainerId, Long roomId, String title, LocalDateTime startTime, LocalDateTime endTime, int maxParticipants) {}
