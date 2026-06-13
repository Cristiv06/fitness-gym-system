package com.fitness.gymservice.service;

import com.fitness.gymservice.dto.TrainerRequest;
import com.fitness.gymservice.dto.TrainerResponse;
import com.fitness.gymservice.dto.TrainerWithUsernameRequest;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TrainerService {
    TrainerResponse create(TrainerRequest request);
    TrainerResponse createWithUsername(TrainerWithUsernameRequest request);
    List<TrainerResponse> findAll();
    Page<TrainerResponse> findAll(Pageable pageable);
    TrainerResponse findById(Long trainerId);
    TrainerResponse findByUsername(String username);
    TrainerResponse update(Long trainerId, TrainerRequest request);
    void delete(Long trainerId);
}
