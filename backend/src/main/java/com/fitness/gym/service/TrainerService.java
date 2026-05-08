package com.fitness.gym.service;

import com.fitness.gym.dto.TrainerRequest;
import com.fitness.gym.dto.TrainerResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TrainerService {

    TrainerResponse create(TrainerRequest request);

    List<TrainerResponse> findAll();

    Page<TrainerResponse> findAll(Pageable pageable);

    TrainerResponse findById(Long trainerId);

    TrainerResponse update(Long trainerId, TrainerRequest request);

    void delete(Long trainerId);
}
