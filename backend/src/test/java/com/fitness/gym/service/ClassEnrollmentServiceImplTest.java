package com.fitness.gym.service;

import com.fitness.gym.dto.ClassEnrollmentRequest;
import com.fitness.gym.dto.ClassEnrollmentResponse;
import com.fitness.gym.entity.ClassEnrollment;
import com.fitness.gym.entity.GymClass;
import com.fitness.gym.entity.Member;
import com.fitness.gym.entity.Room;
import com.fitness.gym.entity.Trainer;
import com.fitness.gym.exception.BadRequestException;
import com.fitness.gym.exception.ConflictException;
import com.fitness.gym.exception.NotFoundException;
import com.fitness.gym.repository.ClassEnrollmentRepository;
import com.fitness.gym.repository.GymClassRepository;
import com.fitness.gym.repository.MemberRepository;
import com.fitness.gym.service.impl.ClassEnrollmentServiceImpl;
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
class ClassEnrollmentServiceImplTest {

    @Mock
    private ClassEnrollmentRepository enrollmentRepository;
    @Mock
    private MemberRepository memberRepository;
    @Mock
    private GymClassRepository gymClassRepository;

    @InjectMocks
    private ClassEnrollmentServiceImpl service;

    private Member member;
    private GymClass gymClass;
    private ClassEnrollment enrollment;
    private ClassEnrollmentRequest request;

    @BeforeEach
    void setUp() {
        Trainer trainer = new Trainer();
        trainer.setTrainerId(1L);
        trainer.setFullName("Trainer A");

        Room room = new Room();
        room.setRoomId(1L);
        room.setName("Room A");
        room.setMaxCapacity(20);

        member = new Member();
        member.setMemberId(1L);
        member.setEmail("test@test.com");
        member.setFullName("Test Member");
        member.setActive(true);
        member.setCreatedAt(LocalDateTime.now());

        gymClass = new GymClass();
        gymClass.setClassId(1L);
        gymClass.setTitle("Yoga");
        gymClass.setMaxParticipants(10);
        gymClass.setTrainer(trainer);
        gymClass.setRoom(room);
        gymClass.setStartTime(LocalDateTime.now().plusDays(1));
        gymClass.setEndTime(LocalDateTime.now().plusDays(1).plusHours(1));

        enrollment = new ClassEnrollment();
        enrollment.setEnrollmentId(1L);
        enrollment.setMember(member);
        enrollment.setGymClass(gymClass);
        enrollment.setEnrolledAt(LocalDateTime.now());

        request = new ClassEnrollmentRequest(1L, 1L);
    }

    @Test
    void create_success() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(gymClassRepository.findById(1L)).thenReturn(Optional.of(gymClass));
        when(enrollmentRepository.findByMember_MemberIdAndGymClass_ClassId(1L, 1L)).thenReturn(Optional.empty());
        when(enrollmentRepository.countByGymClass_ClassId(1L)).thenReturn(5L);
        when(enrollmentRepository.save(any(ClassEnrollment.class))).thenReturn(enrollment);

        ClassEnrollmentResponse result = service.create(request);

        assertThat(result.enrollmentId()).isEqualTo(1L);
        assertThat(result.memberId()).isEqualTo(1L);
        assertThat(result.classId()).isEqualTo(1L);
        verify(enrollmentRepository).save(any(ClassEnrollment.class));
    }

    @Test
    void create_throwsNotFound_whenMemberNotFound() {
        when(memberRepository.findById(99L)).thenReturn(Optional.empty());
        ClassEnrollmentRequest badRequest = new ClassEnrollmentRequest(99L, 1L);

        assertThatThrownBy(() -> service.create(badRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void create_throwsBadRequest_whenMemberInactive() {
        member.setActive(false);
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BadRequestException.class);

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void create_throwsNotFound_whenClassNotFound() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(gymClassRepository.findById(99L)).thenReturn(Optional.empty());
        ClassEnrollmentRequest badRequest = new ClassEnrollmentRequest(1L, 99L);

        assertThatThrownBy(() -> service.create(badRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void create_throwsConflict_whenAlreadyEnrolled() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(gymClassRepository.findById(1L)).thenReturn(Optional.of(gymClass));
        when(enrollmentRepository.findByMember_MemberIdAndGymClass_ClassId(1L, 1L)).thenReturn(Optional.of(enrollment));

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ConflictException.class);

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void create_throwsBadRequest_whenClassFull() {
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(gymClassRepository.findById(1L)).thenReturn(Optional.of(gymClass));
        when(enrollmentRepository.findByMember_MemberIdAndGymClass_ClassId(1L, 1L)).thenReturn(Optional.empty());
        when(enrollmentRepository.countByGymClass_ClassId(1L)).thenReturn(10L);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BadRequestException.class);

        verify(enrollmentRepository, never()).save(any());
    }

    @Test
    void findAll_returnsAll() {
        when(enrollmentRepository.findAll()).thenReturn(List.of(enrollment));

        List<ClassEnrollmentResponse> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).enrollmentId()).isEqualTo(1L);
    }

    @Test
    void findAll_returnsEmptyList() {
        when(enrollmentRepository.findAll()).thenReturn(List.of());

        List<ClassEnrollmentResponse> result = service.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void findById_success() {
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));

        ClassEnrollmentResponse result = service.findById(1L);

        assertThat(result.enrollmentId()).isEqualTo(1L);
    }

    @Test
    void findById_throwsNotFound() {
        when(enrollmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_success() {
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));

        service.delete(1L);

        verify(enrollmentRepository).deleteById(1L);
    }

    @Test
    void delete_throwsNotFound() {
        when(enrollmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_success() {
        when(enrollmentRepository.findById(1L)).thenReturn(Optional.of(enrollment));
        when(memberRepository.findById(1L)).thenReturn(Optional.of(member));
        when(gymClassRepository.findById(1L)).thenReturn(Optional.of(gymClass));
        when(enrollmentRepository.save(any())).thenReturn(enrollment);

        ClassEnrollmentResponse result = service.update(1L, request);

        assertThat(result).isNotNull();
        verify(enrollmentRepository).save(any());
    }
}
