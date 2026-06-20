package com.fitness.gymservice.service.impl;

import com.fitness.gymservice.client.UserServiceClientWrapper;
import com.fitness.gymservice.dto.ClassEnrollmentRequest;
import com.fitness.gymservice.dto.ClassEnrollmentResponse;
import com.fitness.gymservice.dto.MemberResponse;
import com.fitness.gymservice.entity.ClassEnrollment;
import com.fitness.gymservice.entity.GymClass;
import com.fitness.gymservice.exception.BadRequestException;
import com.fitness.gymservice.exception.ConflictException;
import com.fitness.gymservice.exception.NotFoundException;
import com.fitness.gymservice.repository.ClassEnrollmentRepository;
import com.fitness.gymservice.repository.GymClassRepository;
import com.fitness.gymservice.service.ClassEnrollmentService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class ClassEnrollmentServiceImpl implements ClassEnrollmentService {

    private final ClassEnrollmentRepository enrollmentRepository;
    private final GymClassRepository gymClassRepository;
    private final UserServiceClientWrapper userServiceClientWrapper;

    public ClassEnrollmentServiceImpl(ClassEnrollmentRepository enrollmentRepository,
            GymClassRepository gymClassRepository, UserServiceClientWrapper userServiceClientWrapper) {
        this.enrollmentRepository = enrollmentRepository;
        this.gymClassRepository = gymClassRepository;
        this.userServiceClientWrapper = userServiceClientWrapper;
    }

    @Override
    public ClassEnrollmentResponse create(ClassEnrollmentRequest request) {
        // Validate member via user-service (circuit breaker + retry activ)
        MemberResponse member = userServiceClientWrapper.getMemberById(request.memberId());
        if (Boolean.FALSE.equals(member.active())) {
            throw new BadRequestException("Membrul este inactiv si nu se poate inscrie.");
        }

        GymClass gymClass = gymClassRepository.findById(request.classId())
                .orElseThrow(() -> new NotFoundException("Clasa negasita: " + request.classId()));

        enrollmentRepository.findByMemberIdAndGymClass_ClassId(request.memberId(), request.classId())
                .ifPresent(e -> {
                    throw new ConflictException("Membrul este deja inscris la aceasta clasa.");
                });

        long count = enrollmentRepository.countByGymClass_ClassId(request.classId());
        if (count >= gymClass.getMaxParticipants()) {
            throw new BadRequestException("Clasa a atins capacitatea maxima.");
        }

        ClassEnrollment en = new ClassEnrollment();
        en.setMemberId(request.memberId());
        en.setGymClass(gymClass);
        ClassEnrollmentResponse response = toResponse(enrollmentRepository.save(en));
        log.info("Enrollment created: id={}, memberId={}, classId={}", response.enrollmentId(), response.memberId(), response.classId());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassEnrollmentResponse> findAll() {
        return enrollmentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClassEnrollmentResponse findById(Long enrollmentId) {
        return toResponse(load(enrollmentId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ClassEnrollmentResponse> findByMember(Long memberId) {
        return enrollmentRepository.findByMemberId(memberId).stream().map(this::toResponse).toList();
    }

    @Override
    public ClassEnrollmentResponse update(Long enrollmentId, ClassEnrollmentRequest request) {
        ClassEnrollment en = load(enrollmentId);
        if (!en.getMemberId().equals(request.memberId()) || !en.getGymClass().getClassId().equals(request.classId())) {
            enrollmentRepository.findByMemberIdAndGymClass_ClassId(request.memberId(), request.classId())
                    .filter(other -> !other.getEnrollmentId().equals(enrollmentId))
                    .ifPresent(x -> { throw new ConflictException("Exista deja o inscriere pentru acest membru la aceasta clasa."); });
        }
        GymClass gymClass = gymClassRepository.findById(request.classId())
                .orElseThrow(() -> new NotFoundException("Clasa negasita: " + request.classId()));
        en.setMemberId(request.memberId());
        en.setGymClass(gymClass);
        return toResponse(enrollmentRepository.save(en));
    }

    @Override
    public void delete(Long enrollmentId) {
        enrollmentRepository.deleteById(load(enrollmentId).getEnrollmentId());
    }

    private ClassEnrollment load(Long id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Inscriere negasita: " + id));
    }

    private ClassEnrollmentResponse toResponse(ClassEnrollment e) {
        return new ClassEnrollmentResponse(e.getEnrollmentId(), e.getMemberId(), e.getGymClass().getClassId(), e.getEnrolledAt());
    }
}
