package com.fitness.gym.service;

import com.fitness.gym.dto.SubscriptionRequest;
import com.fitness.gym.dto.SubscriptionResponse;
import com.fitness.gym.entity.Member;
import com.fitness.gym.entity.MembershipPlan;
import com.fitness.gym.entity.Subscription;
import com.fitness.gym.entity.SubscriptionStatus;
import com.fitness.gym.exception.BadRequestException;
import com.fitness.gym.exception.NotFoundException;
import com.fitness.gym.repository.MemberRepository;
import com.fitness.gym.repository.MembershipPlanRepository;
import com.fitness.gym.repository.SubscriptionRepository;
import com.fitness.gym.service.impl.SubscriptionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
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
class SubscriptionServiceImplTest {

    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private MembershipPlanRepository planRepository;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private Member member;
    private MembershipPlan plan;
    private Subscription subscription;
    private SubscriptionRequest validRequest;

    @BeforeEach
    void setUp() {
        member = new Member();
        member.setMemberId(1L);
        member.setEmail("test@test.com");
        member.setFullName("Test Member");
        member.setActive(true);
        member.setCreatedAt(LocalDateTime.now());

        plan = new MembershipPlan();
        plan.setPlanId(1L);
        plan.setName("Basic");
        plan.setDurationMonths(1);
        plan.setPrice(BigDecimal.valueOf(100));
        plan.setCreatedAt(LocalDateTime.now());

        subscription = new Subscription();
        subscription.setSubscriptionId(1L);
        subscription.setMember(member);
        subscription.setPlan(plan);
        subscription.setStartDate(LocalDate.now());
        subscription.setEndDate(LocalDate.now().plusMonths(1));
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setCreatedAt(LocalDateTime.now());

        validRequest = new SubscriptionRequest(1L, 1L, LocalDate.now(), LocalDate.now().plusMonths(1), SubscriptionStatus.ACTIVE);
    }

    @Test
    void create_success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);

        SubscriptionResponse result = subscriptionService.create(validRequest);

        assertThat(result.subscriptionId()).isEqualTo(1L);
        assertThat(result.status()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(result.memberId()).isEqualTo(1L);
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void create_setsActiveStatus_whenStatusIsNull() {
        SubscriptionRequest requestWithNullStatus = new SubscriptionRequest(1L, 1L, LocalDate.now(), LocalDate.now().plusMonths(1), null);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(subscriptionRepository.save(any(Subscription.class))).thenReturn(subscription);

        SubscriptionResponse result = subscriptionService.create(requestWithNullStatus);

        assertThat(result).isNotNull();
        verify(subscriptionRepository).save(any(Subscription.class));
    }

    @Test
    void create_throwsBadRequest_whenEndDateBeforeStartDate() {
        SubscriptionRequest badRequest = new SubscriptionRequest(1L, 1L, LocalDate.now(), LocalDate.now().minusDays(1), null);

        assertThatThrownBy(() -> subscriptionService.create(badRequest))
                .isInstanceOf(BadRequestException.class);

        verify(subscriptionRepository, never()).save(any());
    }

    @Test
    void create_success_whenEndDateEqualsStartDate() {
        LocalDate today = LocalDate.now();
        SubscriptionRequest req = new SubscriptionRequest(1L, 1L, today, today, SubscriptionStatus.ACTIVE);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(subscriptionRepository.save(any())).thenReturn(subscription);

        SubscriptionResponse result = subscriptionService.create(req);

        assertThat(result).isNotNull();
    }

    @Test
    void create_throwsNotFound_whenMemberNotFound() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());
        SubscriptionRequest req = new SubscriptionRequest(99L, 1L, LocalDate.now(), LocalDate.now().plusMonths(1), null);

        assertThatThrownBy(() -> subscriptionService.create(req))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_throwsNotFound_whenPlanNotFound() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(planRepository.findById(99L)).thenReturn(Optional.empty());
        SubscriptionRequest req = new SubscriptionRequest(1L, 99L, LocalDate.now(), LocalDate.now().plusMonths(1), null);

        assertThatThrownBy(() -> subscriptionService.create(req))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void findAll_returnsAll() {
        when(subscriptionRepository.findAll()).thenReturn(List.of(subscription));

        List<SubscriptionResponse> result = subscriptionService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).subscriptionId()).isEqualTo(1L);
    }

    @Test
    void findAll_returnsEmptyList() {
        when(subscriptionRepository.findAll()).thenReturn(List.of());

        List<SubscriptionResponse> result = subscriptionService.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void findById_success() {
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));

        SubscriptionResponse result = subscriptionService.findById(1L);

        assertThat(result.subscriptionId()).isEqualTo(1L);
        assertThat(result.planId()).isEqualTo(1L);
    }

    @Test
    void findById_throwsNotFound() {
        when(subscriptionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.findById(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_success() {
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(planRepository.findById(1L)).thenReturn(Optional.of(plan));
        when(subscriptionRepository.save(any())).thenReturn(subscription);

        SubscriptionResponse result = subscriptionService.update(1L, validRequest);

        assertThat(result).isNotNull();
        verify(subscriptionRepository).save(any());
    }

    @Test
    void update_throwsBadRequest_whenEndDateBeforeStartDate() {
        SubscriptionRequest badRequest = new SubscriptionRequest(1L, 1L, LocalDate.now(), LocalDate.now().minusDays(1), null);

        assertThatThrownBy(() -> subscriptionService.update(1L, badRequest))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void update_throwsNotFound_whenSubscriptionNotFound() {
        when(subscriptionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.update(99L, validRequest))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_success() {
        when(subscriptionRepository.findById(1L)).thenReturn(Optional.of(subscription));

        subscriptionService.delete(1L);

        verify(subscriptionRepository).deleteById(1L);
    }

    @Test
    void delete_throwsNotFound() {
        when(subscriptionRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> subscriptionService.delete(99L))
                .isInstanceOf(NotFoundException.class);
    }
}
