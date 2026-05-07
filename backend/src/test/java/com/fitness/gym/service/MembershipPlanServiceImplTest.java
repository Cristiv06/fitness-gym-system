package com.fitness.gym.service;

import com.fitness.gym.dto.MembershipPlanRequest;
import com.fitness.gym.dto.MembershipPlanResponse;
import com.fitness.gym.entity.MembershipPlan;
import com.fitness.gym.exception.BadRequestException;
import com.fitness.gym.exception.NotFoundException;
import com.fitness.gym.repository.MembershipPlanRepository;
import com.fitness.gym.repository.SubscriptionRepository;
import com.fitness.gym.service.impl.MembershipPlanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MembershipPlanServiceImplTest {

    @Mock
    private MembershipPlanRepository repository;
    @Mock
    private SubscriptionRepository subscriptionRepository;

    @InjectMocks
    private MembershipPlanServiceImpl service;

    private MembershipPlan plan;
    private MembershipPlanRequest request;

    @BeforeEach
    void setUp() {
        plan = new MembershipPlan();
        plan.setPlanId(1L);
        plan.setName("Gold");
        plan.setDurationMonths(3);
        plan.setPrice(BigDecimal.valueOf(150.00));
        plan.setDescription("Premium plan");
        plan.setCreatedAt(LocalDateTime.now());

        request = new MembershipPlanRequest("Gold", 3, BigDecimal.valueOf(150.00), "Premium plan");
    }

    @Test
    void create_success() {
        when(repository.save(any(MembershipPlan.class))).thenReturn(plan);

        MembershipPlanResponse result = service.create(request);

        assertThat(result.planId()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Gold");
        assertThat(result.durationMonths()).isEqualTo(3);
        verify(repository).save(any(MembershipPlan.class));
    }

    @Test
    void create_withNullDescription() {
        MembershipPlanRequest noDesc = new MembershipPlanRequest("Silver", 1, BigDecimal.valueOf(50), null);
        MembershipPlan savedPlan = new MembershipPlan();
        savedPlan.setPlanId(2L);
        savedPlan.setName("Silver");
        savedPlan.setDurationMonths(1);
        savedPlan.setPrice(BigDecimal.valueOf(50));
        savedPlan.setCreatedAt(LocalDateTime.now());
        when(repository.save(any(MembershipPlan.class))).thenReturn(savedPlan);

        MembershipPlanResponse result = service.create(noDesc);

        assertThat(result.name()).isEqualTo("Silver");
    }

    @Test
    void findAll_returnsAll() {
        when(repository.findAll()).thenReturn(List.of(plan));

        List<MembershipPlanResponse> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Gold");
    }

    @Test
    void findAll_returnsEmptyList() {
        when(repository.findAll()).thenReturn(List.of());

        List<MembershipPlanResponse> result = service.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void findById_success() {
        when(repository.findById(1L)).thenReturn(Optional.of(plan));

        MembershipPlanResponse result = service.findById(1L);

        assertThat(result.planId()).isEqualTo(1L);
        assertThat(result.price()).isEqualByComparingTo(BigDecimal.valueOf(150.00));
    }

    @Test
    void findById_throwsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_success() {
        MembershipPlanRequest updateRequest = new MembershipPlanRequest("Gold Plus", 6, BigDecimal.valueOf(280), "Updated");
        when(repository.findById(1L)).thenReturn(Optional.of(plan));
        when(repository.save(any(MembershipPlan.class))).thenReturn(plan);

        MembershipPlanResponse result = service.update(1L, updateRequest);

        assertThat(result).isNotNull();
        verify(repository).save(any(MembershipPlan.class));
    }

    @Test
    void update_throwsNotFound_whenPlanDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_success() {
        when(repository.findById(1L)).thenReturn(Optional.of(plan));
        when(subscriptionRepository.existsByPlan_PlanId(1L)).thenReturn(false);

        service.delete(1L);

        verify(repository).delete(plan);
    }

    @Test
    void delete_throwsBadRequest_whenSubscriptionsExist() {
        when(repository.findById(1L)).thenReturn(Optional.of(plan));
        when(subscriptionRepository.existsByPlan_PlanId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(BadRequestException.class);

        verify(repository, never()).delete(any());
    }

    @Test
    void delete_throwsNotFound_whenPlanDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(NotFoundException.class);
    }
}
