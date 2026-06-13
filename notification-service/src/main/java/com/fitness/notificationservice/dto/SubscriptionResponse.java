package com.fitness.notificationservice.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record SubscriptionResponse(Long subscriptionId, Long memberId, Long planId, LocalDate startDate, LocalDate endDate, String status, LocalDateTime createdAt) {}
