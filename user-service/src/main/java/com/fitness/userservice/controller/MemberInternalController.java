package com.fitness.userservice.controller;

import com.fitness.userservice.dto.MemberResponse;
import com.fitness.userservice.dto.SubscriptionResponse;
import com.fitness.userservice.service.MemberService;
import com.fitness.userservice.service.SubscriptionService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/internal")
public class MemberInternalController {

    private final MemberService memberService;
    private final SubscriptionService subscriptionService;

    public MemberInternalController(MemberService memberService, SubscriptionService subscriptionService) {
        this.memberService = memberService;
        this.subscriptionService = subscriptionService;
    }

    @GetMapping("/members/{memberId}")
    public MemberResponse getMemberById(@PathVariable Long memberId) {
        return memberService.findById(memberId);
    }

    @GetMapping("/members")
    public List<MemberResponse> getAllMembers() {
        return memberService.findAll();
    }

    @GetMapping("/subscriptions")
    public List<SubscriptionResponse> getAllSubscriptions() {
        return subscriptionService.findAll();
    }
}
