package com.fitness.notificationservice.client;

import com.fitness.notificationservice.dto.MemberResponse;
import com.fitness.notificationservice.dto.SubscriptionResponse;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/internal/members")
    List<MemberResponse> getAllMembers();

    @GetMapping("/api/internal/subscriptions")
    List<SubscriptionResponse> getAllSubscriptions();
}
