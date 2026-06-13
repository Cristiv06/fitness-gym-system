package com.fitness.gymservice.dto;

import java.time.LocalDateTime;

public record ClassEnrollmentResponse(Long enrollmentId, Long memberId, Long classId, LocalDateTime enrolledAt) {}
