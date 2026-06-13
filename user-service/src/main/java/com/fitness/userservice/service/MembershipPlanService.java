package com.fitness.userservice.service;

import com.fitness.userservice.dto.MembershipPlanRequest;
import com.fitness.userservice.dto.MembershipPlanResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MembershipPlanService {
    MembershipPlanResponse create(MembershipPlanRequest request);
    List<MembershipPlanResponse> findAll();
    Page<MembershipPlanResponse> findAll(Pageable pageable);
    MembershipPlanResponse findById(Long planId);
    MembershipPlanResponse update(Long planId, MembershipPlanRequest request);
    void delete(Long planId);
}
