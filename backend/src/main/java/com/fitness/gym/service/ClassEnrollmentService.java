package com.fitness.gym.service;

import com.fitness.gym.dto.ClassEnrollmentRequest;
import com.fitness.gym.dto.ClassEnrollmentResponse;
import java.util.List;

public interface ClassEnrollmentService {

    ClassEnrollmentResponse create(ClassEnrollmentRequest request);

    List<ClassEnrollmentResponse> findAll();

    ClassEnrollmentResponse findById(Long enrollmentId);

    ClassEnrollmentResponse update(Long enrollmentId, ClassEnrollmentRequest request);

    void delete(Long enrollmentId);
}
