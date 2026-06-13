package com.fitness.gymservice.repository;

import com.fitness.gymservice.entity.ClassEnrollment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassEnrollmentRepository extends JpaRepository<ClassEnrollment, Long> {
    Optional<ClassEnrollment> findByMemberIdAndGymClass_ClassId(Long memberId, Long classId);
    long countByGymClass_ClassId(Long classId);
    List<ClassEnrollment> findByMemberId(Long memberId);
}
