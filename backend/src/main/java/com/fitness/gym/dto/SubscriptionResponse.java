package com.fitness.gym.dto;

import com.fitness.gym.entity.SubscriptionStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SubscriptionResponse(
        Long subscriptionId,
        Long memberId,
        Long planId,
        String planName,
        LocalDate startDate,
        LocalDate endDate,
        SubscriptionStatus status,
        LocalDateTime createdAt) {
}
