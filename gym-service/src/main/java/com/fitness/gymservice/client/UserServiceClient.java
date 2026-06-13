package com.fitness.gymservice.client;

import com.fitness.gymservice.dto.MemberResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/internal/members/{memberId}")
    MemberResponse getMemberById(@PathVariable("memberId") Long memberId);
}
