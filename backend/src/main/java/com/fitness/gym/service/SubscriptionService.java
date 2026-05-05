package com.fitness.gym.service;

import com.fitness.gym.dto.SubscriptionRequest;
import com.fitness.gym.dto.SubscriptionResponse;
import java.util.List;

public interface SubscriptionService {

    SubscriptionResponse create(SubscriptionRequest request);

    List<SubscriptionResponse> findAll();

    SubscriptionResponse findById(Long subscriptionId);

    SubscriptionResponse update(Long subscriptionId, SubscriptionRequest request);

    void delete(Long subscriptionId);
}
