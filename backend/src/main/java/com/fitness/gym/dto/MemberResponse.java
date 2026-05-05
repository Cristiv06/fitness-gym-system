package com.fitness.gym.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record MemberResponse(
        Long memberId,
        String email,
        String fullName,
        String phone,
        LocalDate dateOfBirth,
        Boolean active,
        LocalDateTime createdAt
) {
}
