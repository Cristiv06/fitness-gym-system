package com.fitness.userservice.dto;

import com.fitness.userservice.entity.SubscriptionStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record SubscriptionResponse(
        Long subscriptionId,
        Long memberId,
        Long planId,
        LocalDate startDate,
        LocalDate endDate,
        SubscriptionStatus status,
        LocalDateTime createdAt
) {}
