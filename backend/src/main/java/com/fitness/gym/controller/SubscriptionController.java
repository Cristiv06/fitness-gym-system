package com.fitness.gym.controller;

import com.fitness.gym.dto.SubscriptionRequest;
import com.fitness.gym.dto.SubscriptionResponse;
import com.fitness.gym.service.SubscriptionService;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService service;

    public SubscriptionController(SubscriptionService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionResponse create(@Valid @RequestBody SubscriptionRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<SubscriptionResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{subscriptionId}")
    public SubscriptionResponse findById(@PathVariable Long subscriptionId) {
        return service.findById(subscriptionId);
    }

    @PutMapping("/{subscriptionId}")
    public SubscriptionResponse update(
            @PathVariable Long subscriptionId, @Valid @RequestBody SubscriptionRequest request) {
        return service.update(subscriptionId, request);
    }

    @DeleteMapping("/{subscriptionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long subscriptionId) {
        service.delete(subscriptionId);
    }
}
