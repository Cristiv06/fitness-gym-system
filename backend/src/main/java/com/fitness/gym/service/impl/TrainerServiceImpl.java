package com.fitness.gym.service.impl;

import com.fitness.gym.dto.TrainerRequest;
import com.fitness.gym.dto.TrainerResponse;
import com.fitness.gym.entity.Trainer;
import com.fitness.gym.exception.BadRequestException;
import com.fitness.gym.exception.NotFoundException;
import com.fitness.gym.repository.GymClassRepository;
import com.fitness.gym.repository.TrainerRepository;
import com.fitness.gym.service.TrainerService;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional
public class TrainerServiceImpl implements TrainerService {

    private final TrainerRepository repository;
    private final GymClassRepository gymClassRepository;

    public TrainerServiceImpl(TrainerRepository repository, GymClassRepository gymClassRepository) {
        this.repository = repository;
        this.gymClassRepository = gymClassRepository;
    }

    @Override
    public TrainerResponse create(TrainerRequest request) {
        Trainer t = new Trainer();
        apply(t, request);
        TrainerResponse response = toResponse(repository.save(t));
        log.info("Trainer created: id={}, name={}", response.trainerId(), response.fullName());
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainerResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TrainerResponse> findAll(Pageable pageable) {
        return repository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public TrainerResponse findById(Long trainerId) {
        return toResponse(load(trainerId));
    }

    @Override
    public TrainerResponse update(Long trainerId, TrainerRequest request) {
        Trainer t = load(trainerId);
        apply(t, request);
        log.info("Trainer updated: id={}", trainerId);
        return toResponse(repository.save(t));
    }

    @Override
    public void delete(Long trainerId) {
        load(trainerId);
        if (gymClassRepository.existsByTrainer_TrainerId(trainerId)) {
            log.warn("Cannot delete trainer id={}: assigned to gym classes", trainerId);
            throw new BadRequestException("Nu se poate sterge antrenorul: exista clase asociate.");
        }
        repository.deleteById(trainerId);
        log.info("Trainer deleted: id={}", trainerId);
    }

    private Trainer load(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Antrenor negasit: " + id));
    }

    private void apply(Trainer t, TrainerRequest r) {
        t.setFullName(r.fullName());
        t.setSpecialization(r.specialization());
        t.setPhone(r.phone());
        t.setEmail(r.email());
    }

    private TrainerResponse toResponse(Trainer t) {
        return new TrainerResponse(t.getTrainerId(), t.getFullName(), t.getSpecialization(), t.getPhone(), t.getEmail());
    }
}
