package com.fitness.gym.service;

import com.fitness.gym.dto.MembershipPlanRequest;
import com.fitness.gym.dto.MembershipPlanResponse;
import java.util.List;

public interface MembershipPlanService {

    MembershipPlanResponse create(MembershipPlanRequest request);

    List<MembershipPlanResponse> findAll();

    MembershipPlanResponse findById(Long planId);

    MembershipPlanResponse update(Long planId, MembershipPlanRequest request);

    void delete(Long planId);
}
