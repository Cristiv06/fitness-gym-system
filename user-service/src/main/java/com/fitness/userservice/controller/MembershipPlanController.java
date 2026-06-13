package com.fitness.userservice.controller;

import com.fitness.userservice.dto.MembershipPlanRequest;
import com.fitness.userservice.dto.MembershipPlanResponse;
import com.fitness.userservice.service.MembershipPlanService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/membership-plans")
public class MembershipPlanController {

    private final MembershipPlanService planService;

    public MembershipPlanController(MembershipPlanService planService) {
        this.planService = planService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MembershipPlanResponse create(@Valid @RequestBody MembershipPlanRequest request) {
        return planService.create(request);
    }

    @GetMapping
    public List<MembershipPlanResponse> findAll() {
        return planService.findAll();
    }

    @GetMapping("/page")
    public Page<MembershipPlanResponse> findAllPaged(Pageable pageable) {
        return planService.findAll(pageable);
    }

    @GetMapping("/{planId}")
    public MembershipPlanResponse findById(@PathVariable Long planId) {
        return planService.findById(planId);
    }

    @PutMapping("/{planId}")
    public MembershipPlanResponse update(@PathVariable Long planId,
            @Valid @RequestBody MembershipPlanRequest request) {
        return planService.update(planId, request);
    }

    @DeleteMapping("/{planId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long planId) {
        planService.delete(planId);
    }
}
