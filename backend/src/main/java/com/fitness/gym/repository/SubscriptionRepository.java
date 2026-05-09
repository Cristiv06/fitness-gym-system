package com.fitness.gym.repository;

import com.fitness.gym.entity.Subscription;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubscriptionRepository extends JpaRepository<Subscription, Long> {

    boolean existsByPlan_PlanId(Long planId);

    @Query("SELECT s FROM Subscription s JOIN FETCH s.plan WHERE s.member.username = :username")
    List<Subscription> findByMember_Username(@Param("username") String username);
}
