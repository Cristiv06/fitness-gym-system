package com.fitness.userservice.dto;

import com.fitness.userservice.entity.SubscriptionStatus;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record SubscriptionRequest(
        @NotNull Long memberId,
        @NotNull Long planId,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        SubscriptionStatus status
) {}
