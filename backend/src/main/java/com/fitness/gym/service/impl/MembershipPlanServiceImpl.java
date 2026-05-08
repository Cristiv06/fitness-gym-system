package com.fitness.gym.service.impl;

import com.fitness.gym.dto.MembershipPlanRequest;
import com.fitness.gym.dto.MembershipPlanResponse;
import com.fitness.gym.entity.MembershipPlan;
import com.fitness.gym.exception.BadRequestException;
import com.fitness.gym.exception.NotFoundException;
import com.fitness.gym.repository.MembershipPlanRepository;
import com.fitness.gym.repository.SubscriptionRepository;
import com.fitness.gym.service.MembershipPlanService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class MembershipPlanServiceImpl implements MembershipPlanService {

    private final MembershipPlanRepository repository;
    private final SubscriptionRepository subscriptionRepository;

    public MembershipPlanServiceImpl(MembershipPlanRepository repository, SubscriptionRepository subscriptionRepository) {
        this.repository = repository;
        this.subscriptionRepository = subscriptionRepository;
    }

    @Override
    public MembershipPlanResponse create(MembershipPlanRequest request) {
        MembershipPlan entity = new MembershipPlan();
        apply(entity, request);
        MembershipPlanResponse response = toResponse(repository.save(entity));
        log.info("Membership plan created: id={}, name={}", response.planId(), response.name());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MembershipPlanResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MembershipPlanResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public MembershipPlanResponse findById(Long planId) {
        return toResponse(load(planId));
    }

    @Override
    public MembershipPlanResponse update(Long planId, MembershipPlanRequest request) {
        MembershipPlan entity = load(planId);
        apply(entity, request);
        log.info("Membership plan updated: id={}", planId);
        return toResponse(repository.save(entity));
    }

    @Override
    public void delete(Long planId) {
        MembershipPlan entity = load(planId);
        if (subscriptionRepository.existsByPlan_PlanId(planId)) {
            log.warn("Cannot delete plan id={}: active subscriptions exist", planId);
            throw new BadRequestException("Nu se poate sterge planul: exista abonamente asociate.");
        }
        repository.delete(entity);
        log.info("Membership plan deleted: id={}", planId);
    }

    private MembershipPlan load(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Plan negasit: " + id));
    }

    private void apply(MembershipPlan entity, MembershipPlanRequest request) {
        entity.setName(request.name());
        entity.setDurationMonths(request.durationMonths());
        entity.setPrice(request.price());
        entity.setDescription(request.description());
    }

    private MembershipPlanResponse toResponse(MembershipPlan entity) {
        return new MembershipPlanResponse(
                entity.getPlanId(),
                entity.getName(),
                entity.getDurationMonths(),
                entity.getPrice(),
                entity.getDescription(),
                entity.getCreatedAt());
    }
}
