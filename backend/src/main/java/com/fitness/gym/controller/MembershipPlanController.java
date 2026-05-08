package com.fitness.gym.controller;

import com.fitness.gym.dto.MembershipPlanRequest;
import com.fitness.gym.dto.MembershipPlanResponse;
import com.fitness.gym.service.MembershipPlanService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/membership-plans")
public class MembershipPlanController {

    private final MembershipPlanService service;

    public MembershipPlanController(MembershipPlanService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MembershipPlanResponse create(@Valid @RequestBody MembershipPlanRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<MembershipPlanResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/page")
    public Page<MembershipPlanResponse> findAllPaged(Pageable pageable) {
        return service.findAll(pageable);
    }

    @GetMapping("/{planId}")
    public MembershipPlanResponse findById(@PathVariable Long planId) {
        return service.findById(planId);
    }

    @PutMapping("/{planId}")
    public MembershipPlanResponse update(@PathVariable Long planId, @Valid @RequestBody MembershipPlanRequest request) {
        return service.update(planId, request);
    }

    @DeleteMapping("/{planId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long planId) {
        service.delete(planId);
    }
}
