package com.fitness.userservice.repository;

import com.fitness.userservice.entity.Subscription;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {
    boolean existsByPlan_PlanId(Long planId);
    List<Subscription> findByMember_Username(String username);
}
