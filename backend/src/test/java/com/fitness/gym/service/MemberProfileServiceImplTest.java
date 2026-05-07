package com.fitness.gym.service;

import com.fitness.gym.dto.MemberProfileRequest;
import com.fitness.gym.dto.MemberProfileResponse;
import com.fitness.gym.entity.Member;
import com.fitness.gym.entity.MemberProfile;
import com.fitness.gym.exception.BadRequestException;
import com.fitness.gym.exception.ConflictException;
import com.fitness.gym.exception.NotFoundException;
import com.fitness.gym.repository.MemberProfileRepository;
import com.fitness.gym.repository.MemberRepository;
import com.fitness.gym.service.impl.MemberProfileServiceImpl;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberProfileServiceImplTest {

    @Mock
    private MemberProfileRepository profileRepository;
    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberProfileServiceImpl service;

    private Member member;
    private MemberProfile profile;
    private MemberProfileRequest request;

    @BeforeEach
    void setUp() {
        member = new Member();
        member.setMemberId(1L);
        member.setEmail("profile@test.com");
        member.setFullName("Profile Member");
        member.setActive(true);
        member.setCreatedAt(LocalDateTime.now());

        profile = new MemberProfile();
        profile.setMemberId(1L);
        profile.setMember(member);
        profile.setEmergencyContact("Emergency Contact 0700000000");
        profile.setNotes("Test notes");

        request = new MemberProfileRequest(1L, "Emergency Contact 0700000000", "Test notes");
    }

    @Test
    void create_success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(profileRepository.findById(1L)).thenReturn(Optional.empty());
        when(profileRepository.save(any(MemberProfile.class))).thenReturn(profile);

        MemberProfileResponse result = service.create(request);

        assertThat(result.memberId()).isEqualTo(1L);
        assertThat(result.emergencyContact()).isEqualTo("Emergency Contact 0700000000");
        verify(profileRepository).save(any(MemberProfile.class));
    }

    @Test
    void create_throwsNotFound_whenMemberNotFound() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());
        MemberProfileRequest badRequest = new MemberProfileRequest(99L, null, null);

        assertThatThrownBy(() -> service.create(badRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");

        verify(profileRepository, never()).save(any());
    }

    @Test
    void create_throwsConflict_whenProfileAlreadyExists() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile));

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ConflictException.class);

        verify(profileRepository, never()).save(any());
    }

    @Test
    void findAll_returnsAll() {
        when(profileRepository.findAll()).thenReturn(List.of(profile));

        List<MemberProfileResponse> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).memberId()).isEqualTo(1L);
    }

    @Test
    void findAll_returnsEmptyList() {
        when(profileRepository.findAll()).thenReturn(List.of());

        List<MemberProfileResponse> result = service.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void findByMemberId_success() {
        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile));

        MemberProfileResponse result = service.findByMemberId(1L);

        assertThat(result.memberId()).isEqualTo(1L);
        assertThat(result.notes()).isEqualTo("Test notes");
    }

    @Test
    void findByMemberId_throwsNotFound() {
        when(profileRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findByMemberId(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_success() {
        MemberProfileRequest updateRequest = new MemberProfileRequest(1L, "New Contact", "New notes");
        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile));
        when(profileRepository.save(any(MemberProfile.class))).thenReturn(profile);

        MemberProfileResponse result = service.update(1L, updateRequest);

        assertThat(result).isNotNull();
        verify(profileRepository).save(any(MemberProfile.class));
    }

    @Test
    void update_throwsBadRequest_whenMemberIdMismatch() {
        MemberProfileRequest mismatchRequest = new MemberProfileRequest(2L, "Contact", "Notes");

        assertThatThrownBy(() -> service.update(1L, mismatchRequest))
                .isInstanceOf(BadRequestException.class);
    }

    @Test
    void update_throwsNotFound_whenProfileDoesNotExist() {
        when(profileRepository.findById(99L)).thenReturn(Optional.empty());
        MemberProfileRequest req = new MemberProfileRequest(99L, null, null);

        assertThatThrownBy(() -> service.update(99L, req))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_success() {
        when(profileRepository.findById(1L)).thenReturn(Optional.of(profile));

        service.delete(1L);

        verify(profileRepository).delete(profile);
    }

    @Test
    void delete_throwsNotFound_whenProfileDoesNotExist() {
        when(profileRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(NotFoundException.class);
    }
}
