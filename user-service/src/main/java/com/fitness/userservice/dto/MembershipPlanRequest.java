package com.fitness.userservice.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;

public record MembershipPlanRequest(
        @NotBlank @Size(max = 80) String name,
        @NotNull @Min(1) Integer durationMonths,
        @NotNull @DecimalMin("0") BigDecimal price,
        @Size(max = 255) String description
) {}
