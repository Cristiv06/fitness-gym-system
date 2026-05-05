package com.fitness.gym.dto;

import java.time.LocalDateTime;

public record ClassEnrollmentResponse(Long enrollmentId, Long memberId, Long classId, LocalDateTime enrolledAt) {
}
