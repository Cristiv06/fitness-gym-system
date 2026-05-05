package com.fitness.gym.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public record MembershipPlanRequest(
        @NotBlank @Size(max = 80) String name,
        @NotNull @Min(1) Integer durationMonths,
        @NotNull @DecimalMin("0.0") BigDecimal price,
        @Size(max = 255) String description) {
}
