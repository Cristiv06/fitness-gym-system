package com.fitness.notificationservice.dto;

import java.time.LocalDateTime;
import java.util.Map;

public record ReportSummary(
        String reportType,
        LocalDateTime generatedAt,
        Map<String, Object> data
) {}
