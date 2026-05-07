package com.fitness.gym.service;

import com.fitness.gym.dto.MemberRequest;
import com.fitness.gym.dto.MemberResponse;
import com.fitness.gym.entity.Member;
import com.fitness.gym.exception.ConflictException;
import com.fitness.gym.exception.NotFoundException;
import com.fitness.gym.repository.MemberRepository;
import com.fitness.gym.service.impl.MemberServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class MemberServiceImplTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private MemberServiceImpl memberService;

    private Member member;
    private MemberRequest request;

    @BeforeEach
    void setUp() {
        member = new Member();
        member.setMemberId(1L);
        member.setEmail("test@example.com");
        member.setFullName("Test User");
        member.setPhone("0700000000");
        member.setDateOfBirth(LocalDate.of(1990, 1, 1));
        member.setActive(true);
        member.setCreatedAt(LocalDateTime.now());

        request = new MemberRequest("test@example.com", "Test User", "0700000000", LocalDate.of(1990, 1, 1));
    }

    @Test
    void create_success() {
        when(memberRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        MemberResponse result = memberService.create(request);

        assertThat(result.email()).isEqualTo(request.email());
        assertThat(result.fullName()).isEqualTo(request.fullName());
        assertThat(result.active()).isTrue();
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void create_throwsConflict_whenEmailExists() {
        when(memberRepository.findByEmail(request.email())).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> memberService.create(request))
                .isInstanceOf(ConflictException.class);

        verify(memberRepository, never()).save(any());
    }

    @Test
    void findAll_returnsAllMembers() {
        when(memberRepository.findAll()).thenReturn(List.of(member));

        List<MemberResponse> result = memberService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).email()).isEqualTo(member.getEmail());
    }

    @Test
    void findAll_returnsEmptyList_whenNoMembers() {
        when(memberRepository.findAll()).thenReturn(List.of());

        List<MemberResponse> result = memberService.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void findById_success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        MemberResponse result = memberService.findById(1L);

        assertThat(result.memberId()).isEqualTo(1L);
        assertThat(result.email()).isEqualTo(member.getEmail());
    }

    @Test
    void findById_throwsNotFound_whenMemberDoesNotExist() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.findById(99L))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void update_success() {
        MemberRequest updateRequest = new MemberRequest("new@example.com", "New Name", null, null);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        memberService.update(1L, updateRequest);

        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void update_success_whenSameEmailSameMember() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberRepository.findByEmail(request.email())).thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        MemberResponse result = memberService.update(1L, request);

        assertThat(result).isNotNull();
        verify(memberRepository).save(any(Member.class));
    }

    @Test
    void update_throwsNotFound_whenMemberDoesNotExist() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.update(99L, request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_throwsConflict_whenEmailBelongsToAnotherMember() {
        Member other = new Member();
        other.setMemberId(2L);
        other.setEmail("other@example.com");

        MemberRequest updateRequest = new MemberRequest("other@example.com", "Name", null, null);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberRepository.findByEmail("other@example.com")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> memberService.update(1L, updateRequest))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void deactivate_setsActiveToFalse() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(memberRepository.save(any(Member.class))).thenReturn(member);

        memberService.deactivate(1L);

        assertThat(member.getActive()).isFalse();
        verify(memberRepository).save(member);
    }

    @Test
    void deactivate_throwsNotFound_whenMemberDoesNotExist() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> memberService.deactivate(99L))
                .isInstanceOf(NotFoundException.class);
    }
}
