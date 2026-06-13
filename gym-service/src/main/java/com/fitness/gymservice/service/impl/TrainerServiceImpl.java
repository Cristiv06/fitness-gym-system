package com.fitness.gymservice.service.impl;

import com.fitness.gymservice.dto.TrainerRequest;
import com.fitness.gymservice.dto.TrainerResponse;
import com.fitness.gymservice.dto.TrainerWithUsernameRequest;
import com.fitness.gymservice.entity.Trainer;
import com.fitness.gymservice.exception.BadRequestException;
import com.fitness.gymservice.exception.NotFoundException;
import com.fitness.gymservice.repository.GymClassRepository;
import com.fitness.gymservice.repository.TrainerRepository;
import com.fitness.gymservice.service.TrainerService;
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
        t.setFullName(request.fullName());
        t.setSpecialization(request.specialization());
        t.setPhone(request.phone());
        t.setEmail(request.email());
        return toResponse(repository.save(t));
    }

    @Override
    public TrainerResponse createWithUsername(TrainerWithUsernameRequest request) {
        repository.findByEmail(request.email()).ifPresent(existing -> {
            throw new BadRequestException("Exista deja antrenor cu acest email.");
        });
        Trainer t = new Trainer();
        t.setUsername(request.username());
        t.setFullName(request.fullName());
        t.setSpecialization(request.specialization());
        t.setPhone(request.phone());
        t.setEmail(request.email());
        return toResponse(repository.save(t));
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
    @Transactional(readOnly = true)
    public TrainerResponse findByUsername(String username) {
        return repository.findByUsername(username)
                .map(this::toResponse)
                .orElseThrow(() -> new NotFoundException("Antrenor negasit cu username: " + username));
    }

    @Override
    public TrainerResponse update(Long trainerId, TrainerRequest request) {
        Trainer t = load(trainerId);
        t.setFullName(request.fullName());
        t.setSpecialization(request.specialization());
        t.setPhone(request.phone());
        t.setEmail(request.email());
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

    private TrainerResponse toResponse(Trainer t) {
        return new TrainerResponse(t.getTrainerId(), t.getFullName(), t.getSpecialization(), t.getPhone(), t.getEmail());
    }
}
