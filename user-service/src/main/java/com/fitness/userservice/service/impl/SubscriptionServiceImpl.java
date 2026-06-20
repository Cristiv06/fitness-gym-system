package com.fitness.userservice.service.impl;

import com.fitness.userservice.dto.SubscriptionRequest;
import com.fitness.userservice.dto.SubscriptionResponse;
import com.fitness.userservice.entity.Member;
import com.fitness.userservice.entity.MembershipPlan;
import com.fitness.userservice.entity.Subscription;
import com.fitness.userservice.entity.SubscriptionStatus;
import com.fitness.userservice.exception.BadRequestException;
import com.fitness.userservice.exception.NotFoundException;
import com.fitness.userservice.repository.MemberRepository;
import com.fitness.userservice.repository.MembershipPlanRepository;
import com.fitness.userservice.repository.SubscriptionRepository;
import com.fitness.userservice.service.SubscriptionService;
import java.time.LocalDate;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final MemberRepository memberRepository;
    private final MembershipPlanRepository planRepository;

    public SubscriptionServiceImpl(SubscriptionRepository subscriptionRepository,
            MemberRepository memberRepository, MembershipPlanRepository planRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.memberRepository = memberRepository;
        this.planRepository = planRepository;
    }

    @Override
    public SubscriptionResponse create(SubscriptionRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new NotFoundException("Membru negasit: " + request.memberId()));
        MembershipPlan plan = planRepository.findById(request.planId())
                .orElseThrow(() -> new NotFoundException("Plan negasit: " + request.planId()));
        LocalDate endDate = resolveEndDate(request, plan);
        Subscription s = new Subscription();
        s.setMember(member);
        s.setPlan(plan);
        s.setStartDate(request.startDate());
        s.setEndDate(endDate);
        s.setStatus(request.status() != null ? request.status() : SubscriptionStatus.ACTIVE);
        return toResponse(subscriptionRepository.save(s));
    }

    @Override
    @Transactional(readOnly = true)
    public List<SubscriptionResponse> findAll() {
        return subscriptionRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SubscriptionResponse findById(Long subscriptionId) {
        return toResponse(load(subscriptionId));
    }

    @Override
    public SubscriptionResponse update(Long subscriptionId, SubscriptionRequest request) {
        Subscription s = load(subscriptionId);
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new NotFoundException("Membru negasit: " + request.memberId()));
        MembershipPlan plan = planRepository.findById(request.planId())
                .orElseThrow(() -> new NotFoundException("Plan negasit: " + request.planId()));
        LocalDate endDate = resolveEndDate(request, plan);
        s.setMember(member);
        s.setPlan(plan);
        s.setStartDate(request.startDate());
        s.setEndDate(endDate);
        s.setStatus(request.status() != null ? request.status() : s.getStatus());
        return toResponse(subscriptionRepository.save(s));
    }

    @Override
    public void delete(Long subscriptionId) {
        subscriptionRepository.deleteById(load(subscriptionId).getSubscriptionId());
    }

    private LocalDate resolveEndDate(SubscriptionRequest request, MembershipPlan plan) {
        LocalDate endDate = request.endDate() != null
                ? request.endDate()
                : request.startDate().plusMonths(plan.getDurationMonths());
        if (endDate.isBefore(request.startDate())) {
            throw new BadRequestException("Data sfarsit trebuie sa fie >= data start.");
        }
        return endDate;
    }

    private Subscription load(Long id) {
        return subscriptionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Abonament negasit: " + id));
    }

    private SubscriptionResponse toResponse(Subscription s) {
        return new SubscriptionResponse(s.getSubscriptionId(), s.getMember().getMemberId(),
                s.getPlan().getPlanId(), s.getPlan().getName(), s.getStartDate(), s.getEndDate(),
                s.getStatus(), s.getCreatedAt());
    }
}
