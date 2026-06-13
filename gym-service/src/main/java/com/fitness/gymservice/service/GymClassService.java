package com.fitness.gymservice.service;

import com.fitness.gymservice.dto.GymClassRequest;
import com.fitness.gymservice.dto.GymClassResponse;
import java.util.List;

public interface GymClassService {
    GymClassResponse create(GymClassRequest request);
    List<GymClassResponse> findAll();
    GymClassResponse findById(Long classId);
    List<GymClassResponse> findByTrainer(Long trainerId);
    List<GymClassResponse> findAvailable();
    List<GymClassResponse> findEnrolledByMember(Long memberId);
    GymClassResponse update(Long classId, GymClassRequest request);
    void delete(Long classId);
}
