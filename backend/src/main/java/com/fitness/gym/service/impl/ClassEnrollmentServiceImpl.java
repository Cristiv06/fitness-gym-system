package com.fitness.gym.service.impl;

import com.fitness.gym.dto.ClassEnrollmentRequest;
import com.fitness.gym.dto.ClassEnrollmentResponse;
import com.fitness.gym.entity.ClassEnrollment;
import com.fitness.gym.entity.GymClass;
import com.fitness.gym.entity.Member;
import com.fitness.gym.exception.BadRequestException;
import com.fitness.gym.exception.ConflictException;
import com.fitness.gym.exception.NotFoundException;
import com.fitness.gym.repository.ClassEnrollmentRepository;
import com.fitness.gym.repository.GymClassRepository;
import com.fitness.gym.repository.MemberRepository;
import com.fitness.gym.service.ClassEnrollmentService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ClassEnrollmentServiceImpl implements ClassEnrollmentService {

    private final ClassEnrollmentRepository enrollmentRepository;
    private final MemberRepository memberRepository;
    private final GymClassRepository gymClassRepository;

    public ClassEnrollmentServiceImpl(
            ClassEnrollmentRepository enrollmentRepository,
            MemberRepository memberRepository,
            GymClassRepository gymClassRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.memberRepository = memberRepository;
        this.gymClassRepository = gymClassRepository;
    }

    @Override
    public ClassEnrollmentResponse create(ClassEnrollmentRequest request) {
        Member member = memberRepository
                .findById(request.memberId())
                .orElseThrow(() -> new NotFoundException("Membru negasit: " + request.memberId()));
        if (Boolean.FALSE.equals(member.getActive())) {
            throw new BadRequestException("Membrul este inactiv si nu se poate inscrie.");
        }
        GymClass gymClass = gymClassRepository
                .findById(request.classId())
                .orElseThrow(() -> new NotFoundException("Clasa negasita: " + request.classId()));
        enrollmentRepository
                .findByMember_MemberIdAndGymClass_ClassId(request.memberId(), request.classId())
                .ifPresent(e -> {
                    throw new ConflictException("Membrul este deja inscris la aceasta clasa.");
                });
        long count = enrollmentRepository.countByGymClass_ClassId(request.classId());
        if (count >= gymClass.getMaxParticipants()) {
            throw new BadRequestException("Clasa a atins capacitatea maxima.");
        }
        ClassEnrollment en = new ClassEnrollment();
        en.setMember(member);
        en.setGymClass(gymClass);
        return toResponse(enrollmentRepository.save(en));
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
    public ClassEnrollmentResponse update(Long enrollmentId, ClassEnrollmentRequest request) {
        ClassEnrollment en = load(enrollmentId);
        if (!en.getMember().getMemberId().equals(request.memberId()) || !en.getGymClass().getClassId().equals(request.classId())) {
            enrollmentRepository
                    .findByMember_MemberIdAndGymClass_ClassId(request.memberId(), request.classId())
                    .filter(other -> !other.getEnrollmentId().equals(enrollmentId))
                    .ifPresent(x -> {
                        throw new ConflictException("Exista deja o inscriere pentru acest membru la aceasta clasa.");
                    });
        }
        Member member = memberRepository
                .findById(request.memberId())
                .orElseThrow(() -> new NotFoundException("Membru negasit: " + request.memberId()));
        GymClass gymClass = gymClassRepository
                .findById(request.classId())
                .orElseThrow(() -> new NotFoundException("Clasa negasita: " + request.classId()));
        en.setMember(member);
        en.setGymClass(gymClass);
        return toResponse(enrollmentRepository.save(en));
    }

    @Override
    public void delete(Long enrollmentId) {
        enrollmentRepository.deleteById(load(enrollmentId).getEnrollmentId());
    }

    private ClassEnrollment load(Long id) {
        return enrollmentRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Inscriere negasita: " + id));
    }

    private ClassEnrollmentResponse toResponse(ClassEnrollment e) {
        return new ClassEnrollmentResponse(
                e.getEnrollmentId(), e.getMember().getMemberId(), e.getGymClass().getClassId(), e.getEnrolledAt());
    }
}
