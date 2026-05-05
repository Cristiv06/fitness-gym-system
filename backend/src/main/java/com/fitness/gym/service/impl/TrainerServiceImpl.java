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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        return toResponse(repository.save(t));
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainerResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
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
        return toResponse(repository.save(t));
    }

    @Override
    public void delete(Long trainerId) {
        load(trainerId);
        if (gymClassRepository.existsByTrainer_TrainerId(trainerId)) {
            throw new BadRequestException("Nu se poate sterge antrenorul: exista clase asociate.");
        }
        repository.deleteById(trainerId);
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
