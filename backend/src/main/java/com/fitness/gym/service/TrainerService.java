package com.fitness.gym.service;

import com.fitness.gym.dto.TrainerRequest;
import com.fitness.gym.dto.TrainerResponse;
import java.util.List;

public interface TrainerService {

    TrainerResponse create(TrainerRequest request);

    List<TrainerResponse> findAll();

    TrainerResponse findById(Long trainerId);

    TrainerResponse update(Long trainerId, TrainerRequest request);

    void delete(Long trainerId);
}
