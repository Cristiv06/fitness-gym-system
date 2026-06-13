package com.fitness.gymservice.service;

import com.fitness.gymservice.dto.ClassEnrollmentRequest;
import com.fitness.gymservice.dto.ClassEnrollmentResponse;
import java.util.List;

public interface ClassEnrollmentService {
    ClassEnrollmentResponse create(ClassEnrollmentRequest request);
    List<ClassEnrollmentResponse> findAll();
    ClassEnrollmentResponse findById(Long enrollmentId);
    List<ClassEnrollmentResponse> findByMember(Long memberId);
    ClassEnrollmentResponse update(Long enrollmentId, ClassEnrollmentRequest request);
    void delete(Long enrollmentId);
}
