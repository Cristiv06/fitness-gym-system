package com.fitness.gym.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MembershipPlanResponse(
        Long planId, String name, int durationMonths, BigDecimal price, String description, LocalDateTime createdAt) {
}
