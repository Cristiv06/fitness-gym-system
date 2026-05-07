package com.fitness.gym.service;

import com.fitness.gym.dto.CheckInRequest;
import com.fitness.gym.dto.CheckInResponse;
import com.fitness.gym.entity.CheckIn;
import com.fitness.gym.entity.Member;
import com.fitness.gym.exception.NotFoundException;
import com.fitness.gym.repository.CheckInRepository;
import com.fitness.gym.repository.MemberRepository;
import com.fitness.gym.service.impl.CheckInServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CheckInServiceImplTest {

    @Mock
    private CheckInRepository checkInRepository;
    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private CheckInServiceImpl service;

    private Member member;
    private CheckIn checkIn;
    private final LocalDateTime checkinTime = LocalDateTime.of(2026, 5, 1, 10, 0);

    @BeforeEach
    void setUp() {
        member = new Member();
        member.setMemberId(1L);
        member.setEmail("member@test.com");
        member.setFullName("Test Member");
        member.setActive(true);
        member.setCreatedAt(LocalDateTime.now());

        checkIn = new CheckIn();
        checkIn.setCheckinId(1L);
        checkIn.setMember(member);
        checkIn.setCheckinTime(checkinTime);
    }

    @Test
    void create_success_withExplicitCheckinTime() {
        CheckInRequest request = new CheckInRequest(1L, checkinTime);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(checkInRepository.save(any(CheckIn.class))).thenReturn(checkIn);

        CheckInResponse result = service.create(request);

        assertThat(result.checkinId()).isEqualTo(1L);
        assertThat(result.memberId()).isEqualTo(1L);
        verify(checkInRepository).save(any(CheckIn.class));
    }

    @Test
    void create_success_withNullCheckinTime_usesCurrentTime() {
        CheckInRequest request = new CheckInRequest(1L, null);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(checkInRepository.save(any(CheckIn.class))).thenReturn(checkIn);

        CheckInResponse result = service.create(request);

        assertThat(result).isNotNull();
        verify(checkInRepository).save(any(CheckIn.class));
    }

    @Test
    void create_throwsNotFound_whenMemberNotFound() {
        CheckInRequest request = new CheckInRequest(99L, null);
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void findAll_returnsAll() {
        when(checkInRepository.findAll()).thenReturn(List.of(checkIn));

        List<CheckInResponse> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).checkinId()).isEqualTo(1L);
    }

    @Test
    void findAll_returnsEmptyList() {
        when(checkInRepository.findAll()).thenReturn(List.of());

        List<CheckInResponse> result = service.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void findById_success() {
        when(checkInRepository.findById(1L)).thenReturn(Optional.of(checkIn));

        CheckInResponse result = service.findById(1L);

        assertThat(result.checkinId()).isEqualTo(1L);
        assertThat(result.checkinTime()).isEqualTo(checkinTime);
    }

    @Test
    void findById_throwsNotFound() {
        when(checkInRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_success_withNewCheckinTime() {
        LocalDateTime newTime = LocalDateTime.of(2026, 5, 2, 9, 0);
        CheckInRequest request = new CheckInRequest(1L, newTime);
        when(checkInRepository.findById(1L)).thenReturn(Optional.of(checkIn));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(checkInRepository.save(any(CheckIn.class))).thenReturn(checkIn);

        CheckInResponse result = service.update(1L, request);

        assertThat(result).isNotNull();
        verify(checkInRepository).save(any(CheckIn.class));
    }

    @Test
    void update_success_withNullCheckinTime_keepsExistingTime() {
        CheckInRequest request = new CheckInRequest(1L, null);
        when(checkInRepository.findById(1L)).thenReturn(Optional.of(checkIn));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(checkInRepository.save(any(CheckIn.class))).thenReturn(checkIn);

        CheckInResponse result = service.update(1L, request);

        assertThat(result).isNotNull();
    }

    @Test
    void update_throwsNotFound_whenCheckInNotFound() {
        CheckInRequest request = new CheckInRequest(1L, checkinTime);
        when(checkInRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_success() {
        when(checkInRepository.findById(1L)).thenReturn(Optional.of(checkIn));

        service.delete(1L);

        verify(checkInRepository).deleteById(1L);
    }

    @Test
    void delete_throwsNotFound() {
        when(checkInRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(NotFoundException.class);
    }
}
