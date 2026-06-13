package com.fitness.userservice.service.impl;

import com.fitness.userservice.dto.MembershipPlanRequest;
import com.fitness.userservice.dto.MembershipPlanResponse;
import com.fitness.userservice.entity.MembershipPlan;
import com.fitness.userservice.exception.BadRequestException;
import com.fitness.userservice.exception.NotFoundException;
import com.fitness.userservice.repository.MembershipPlanRepository;
import com.fitness.userservice.repository.SubscriptionRepository;
import com.fitness.userservice.service.MembershipPlanService;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MembershipPlanServiceImpl implements MembershipPlanService {

    private final MembershipPlanRepository planRepository;
    private final SubscriptionRepository subscriptionRepository;

    public MembershipPlanServiceImpl(MembershipPlanRepository planRepository, SubscriptionRepository subscriptionRepository) {
        this.planRepository = planRepository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public MembershipPlanResponse create(MembershipPlanRequest request) {
        MembershipPlan plan = new MembershipPlan();
        apply(plan, request);
        return toResponse(planRepository.save(plan));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembershipPlanResponse> findAll() {
        return planRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MembershipPlanResponse> findAll(Pageable pageable) {
        return planRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public MembershipPlanResponse findById(Long planId) {
        return toResponse(load(planId));
    }

    @Override
    public MembershipPlanResponse update(Long planId, MembershipPlanRequest request) {
        MembershipPlan plan = load(planId);
        apply(plan, request);
        return toResponse(planRepository.save(plan));
    }

    @Override
    public void delete(Long planId) {
        load(planId);
        if (subscriptionRepository.existsByPlan_PlanId(planId)) {
            throw new BadRequestException("Nu se poate sterge planul: exista abonamente asociate.");
        }
        planRepository.deleteById(planId);
    }

    private MembershipPlan load(Long id) {
        return planRepository.findById(id).orElseThrow(() -> new NotFoundException("Plan not found: " + id));
    }

    private void apply(MembershipPlan plan, MembershipPlanRequest request) {
        plan.setName(request.name());
        plan.setDurationMonths(request.durationMonths());
        plan.setPrice(request.price());
        plan.setDescription(request.description());
    }

    private MembershipPlanResponse toResponse(MembershipPlan p) {
        return new MembershipPlanResponse(p.getPlanId(), p.getName(), p.getDurationMonths(), p.getPrice(), p.getDescription(), p.getCreatedAt());
    }
}
