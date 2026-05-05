package com.fitness.gym.service;

import com.fitness.gym.dto.GymClassRequest;
import com.fitness.gym.dto.GymClassResponse;
import java.util.List;

public interface GymClassService {

    GymClassResponse create(GymClassRequest request);

    List<GymClassResponse> findAll();

    GymClassResponse findById(Long classId);

    GymClassResponse update(Long classId, GymClassRequest request);

    void delete(Long classId);
}
