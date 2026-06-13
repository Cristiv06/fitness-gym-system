package com.fitness.notificationservice.controller;

import com.fitness.notificationservice.dto.ReportSummary;
import com.fitness.notificationservice.service.ReportService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/members")
    public ReportSummary memberSummary() {
        return reportService.getMemberSummary();
    }

    @GetMapping("/subscriptions")
    public ReportSummary subscriptionSummary() {
        return reportService.getSubscriptionSummary();
    }

    @GetMapping("/classes")
    public ReportSummary classSummary() {
        return reportService.getClassSummary();
    }

    @GetMapping("/check-ins")
    public ReportSummary checkInSummary() {
        return reportService.getCheckInSummary();
    }
}
