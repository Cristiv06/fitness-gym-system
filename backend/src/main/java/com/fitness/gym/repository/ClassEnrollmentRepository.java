package com.fitness.gym.repository;

import com.fitness.gym.entity.ClassEnrollment;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClassEnrollmentRepository extends JpaRepository<ClassEnrollment, Long> {

    Optional<ClassEnrollment> findByMember_MemberIdAndGymClass_ClassId(Long memberId, Long classId);

    long countByGymClass_ClassId(Long classId);

    List<ClassEnrollment> findByMember_Username(String username);
}
