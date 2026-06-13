package com.fitness.userservice.dto;

import java.time.LocalDateTime;

public record GymClassRequest(Long trainerId, Long roomId, String title, LocalDateTime startTime, LocalDateTime endTime, Integer maxParticipants) {}
