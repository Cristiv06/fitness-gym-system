package com.fitness.userservice.controller;

import com.fitness.userservice.dto.SubscriptionRequest;
import com.fitness.userservice.dto.SubscriptionResponse;
import com.fitness.userservice.service.SubscriptionService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionResponse create(@Valid @RequestBody SubscriptionRequest request) {
        return subscriptionService.create(request);
    }

    @GetMapping
    public List<SubscriptionResponse> findAll() {
        return subscriptionService.findAll();
    }

    @GetMapping("/{subscriptionId}")
    public SubscriptionResponse findById(@PathVariable Long subscriptionId) {
        return subscriptionService.findById(subscriptionId);
    }

    @PutMapping("/{subscriptionId}")
    public SubscriptionResponse update(@PathVariable Long subscriptionId,
            @Valid @RequestBody SubscriptionRequest request) {
        return subscriptionService.update(subscriptionId, request);
    }

    @DeleteMapping("/{subscriptionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long subscriptionId) {
        subscriptionService.delete(subscriptionId);
    }
}
