package com.fitness.userservice.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MembershipPlanResponse(
        Long planId,
        String name,
        Integer durationMonths,
        BigDecimal price,
        String description,
        LocalDateTime createdAt
) {}
