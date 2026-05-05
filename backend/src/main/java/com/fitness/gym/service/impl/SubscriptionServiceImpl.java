package com.fitness.gym.service.impl;

import com.fitness.gym.dto.SubscriptionRequest;
import com.fitness.gym.dto.SubscriptionResponse;
import com.fitness.gym.entity.Member;
import com.fitness.gym.entity.MembershipPlan;
import com.fitness.gym.entity.Subscription;
import com.fitness.gym.entity.SubscriptionStatus;
import com.fitness.gym.exception.BadRequestException;
import com.fitness.gym.exception.NotFoundException;
import com.fitness.gym.repository.MemberRepository;
import com.fitness.gym.repository.MembershipPlanRepository;
import com.fitness.gym.repository.SubscriptionRepository;
import com.fitness.gym.service.SubscriptionService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final MemberRepository memberRepository;
    private final MembershipPlanRepository planRepository;

    public SubscriptionServiceImpl(
            SubscriptionRepository subscriptionRepository,
            MemberRepository memberRepository,
            MembershipPlanRepository planRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.memberRepository = memberRepository;
        this.planRepository = planRepository;
    }

    @Override
    public SubscriptionResponse create(SubscriptionRequest request) {
        validateDates(request);
        Member member = memberRepository
                .findById(request.memberId())
                .orElseThrow(() -> new NotFoundException("Membru negasit: " + request.memberId()));
        MembershipPlan plan = planRepository
                .findById(request.planId())
                .orElseThrow(() -> new NotFoundException("Plan negasit: " + request.planId()));
        Subscription s = new Subscription();
        s.setMember(member);
        s.setPlan(plan);
        s.setStartDate(request.startDate());
        s.setEndDate(request.endDate());
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
        validateDates(request);
        Subscription s = load(subscriptionId);
        Member member = memberRepository
                .findById(request.memberId())
                .orElseThrow(() -> new NotFoundException("Membru negasit: " + request.memberId()));
        MembershipPlan plan = planRepository
                .findById(request.planId())
                .orElseThrow(() -> new NotFoundException("Plan negasit: " + request.planId()));
        s.setMember(member);
        s.setPlan(plan);
        s.setStartDate(request.startDate());
        s.setEndDate(request.endDate());
        s.setStatus(request.status() != null ? request.status() : s.getStatus());
        return toResponse(subscriptionRepository.save(s));
    }

    @Override
    public void delete(Long subscriptionId) {
        subscriptionRepository.deleteById(load(subscriptionId).getSubscriptionId());
    }

    private void validateDates(SubscriptionRequest request) {
        if (request.endDate().isBefore(request.startDate())) {
            throw new BadRequestException("Data sfarsit trebuie sa fie >= data start.");
        }
    }

    private Subscription load(Long id) {
        return subscriptionRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Abonament negasit: " + id));
    }

    private SubscriptionResponse toResponse(Subscription s) {
        return new SubscriptionResponse(
                s.getSubscriptionId(),
                s.getMember().getMemberId(),
                s.getPlan().getPlanId(),
                s.getStartDate(),
                s.getEndDate(),
                s.getStatus(),
                s.getCreatedAt());
    }
}
