package com.fitness.notificationservice.service;

import com.fitness.notificationservice.client.GymServiceClient;
import com.fitness.notificationservice.client.UserServiceClient;
import com.fitness.notificationservice.dto.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    private final UserServiceClient userServiceClient;
    private final GymServiceClient gymServiceClient;

    public ReportService(UserServiceClient userServiceClient, GymServiceClient gymServiceClient) {
        this.userServiceClient = userServiceClient;
        this.gymServiceClient = gymServiceClient;
    }

    public ReportSummary getMemberSummary() {
        List<MemberResponse> members = userServiceClient.getAllMembers();
        long totalMembers = members.size();
        long activeMembers = members.stream().filter(m -> Boolean.TRUE.equals(m.active())).count();

        return new ReportSummary("MEMBER_SUMMARY", LocalDateTime.now(), Map.of(
                "totalMembers", totalMembers,
                "activeMembers", activeMembers,
                "inactiveMembers", totalMembers - activeMembers
        ));
    }

    public ReportSummary getSubscriptionSummary() {
        List<SubscriptionResponse> subscriptions = userServiceClient.getAllSubscriptions();
        Map<String, Long> byStatus = subscriptions.stream()
                .collect(Collectors.groupingBy(SubscriptionResponse::status, Collectors.counting()));

        LocalDate today = LocalDate.now();
        long expiringIn30Days = subscriptions.stream()
                .filter(s -> "ACTIVE".equals(s.status()))
                .filter(s -> !s.endDate().isBefore(today) && !s.endDate().isAfter(today.plusDays(30)))
                .count();

        return new ReportSummary("SUBSCRIPTION_SUMMARY", LocalDateTime.now(), Map.of(
                "totalSubscriptions", (long) subscriptions.size(),
                "byStatus", byStatus,
                "expiringIn30Days", expiringIn30Days
        ));
    }

    public ReportSummary getClassSummary() {
        List<GymClassResponse> classes = gymServiceClient.getAllClasses();
        long upcoming = classes.stream()
                .filter(c -> c.startTime().isAfter(LocalDateTime.now()))
                .count();

        return new ReportSummary("CLASS_SUMMARY", LocalDateTime.now(), Map.of(
                "totalClasses", (long) classes.size(),
                "upcomingClasses", upcoming
        ));
    }

    public ReportSummary getCheckInSummary() {
        List<CheckInResponse> checkIns = gymServiceClient.getAllCheckIns();
        LocalDateTime last24h = LocalDateTime.now().minusHours(24);
        long recentCheckIns = checkIns.stream()
                .filter(c -> c.checkinTime().isAfter(last24h))
                .count();

        Map<Long, Long> checkInsByMember = checkIns.stream()
                .collect(Collectors.groupingBy(CheckInResponse::memberId, Collectors.counting()));

        return new ReportSummary("CHECKIN_SUMMARY", LocalDateTime.now(), Map.of(
                "totalCheckIns", (long) checkIns.size(),
                "checkInsLast24h", recentCheckIns,
                "uniqueMembers", (long) checkInsByMember.size()
        ));
    }
}
