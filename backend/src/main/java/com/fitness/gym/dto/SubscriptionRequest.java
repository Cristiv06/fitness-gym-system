package com.fitness.gym.dto;

import com.fitness.gym.entity.SubscriptionStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record SubscriptionRequest(
        @NotNull Long memberId,
        @NotNull Long planId,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        SubscriptionStatus status) {
}
