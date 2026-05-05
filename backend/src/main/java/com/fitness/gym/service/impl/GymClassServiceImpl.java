package com.fitness.gym.service.impl;

import com.fitness.gym.dto.GymClassRequest;
import com.fitness.gym.dto.GymClassResponse;
import com.fitness.gym.entity.GymClass;
import com.fitness.gym.entity.Room;
import com.fitness.gym.entity.Trainer;
import com.fitness.gym.exception.BadRequestException;
import com.fitness.gym.exception.NotFoundException;
import com.fitness.gym.repository.GymClassRepository;
import com.fitness.gym.repository.RoomRepository;
import com.fitness.gym.repository.TrainerRepository;
import com.fitness.gym.service.GymClassService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class GymClassServiceImpl implements GymClassService {

    private final GymClassRepository gymClassRepository;
    private final TrainerRepository trainerRepository;
    private final RoomRepository roomRepository;

    public GymClassServiceImpl(
            GymClassRepository gymClassRepository, TrainerRepository trainerRepository, RoomRepository roomRepository) {
        this.gymClassRepository = gymClassRepository;
        this.trainerRepository = trainerRepository;
        this.roomRepository = roomRepository;
    }

    @Override
    public GymClassResponse create(GymClassRequest request) {
        validateSchedule(request);
        Trainer trainer = trainerRepository
                .findById(request.trainerId())
                .orElseThrow(() -> new NotFoundException("Antrenor negasit: " + request.trainerId()));
        Room room = roomRepository
                .findById(request.roomId())
                .orElseThrow(() -> new NotFoundException("Sala negasita: " + request.roomId()));
        GymClass gc = new GymClass();
        gc.setTrainer(trainer);
        gc.setRoom(room);
        gc.setTitle(request.title());
        gc.setStartTime(request.startTime());
        gc.setEndTime(request.endTime());
        gc.setMaxParticipants(request.maxParticipants());
        return toResponse(gymClassRepository.save(gc));
    }

    @Override
    @Transactional(readOnly = true)
    public List<GymClassResponse> findAll() {
        return gymClassRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public GymClassResponse findById(Long classId) {
        return toResponse(load(classId));
    }

    @Override
    public GymClassResponse update(Long classId, GymClassRequest request) {
        validateSchedule(request);
        GymClass gc = load(classId);
        Trainer trainer = trainerRepository
                .findById(request.trainerId())
                .orElseThrow(() -> new NotFoundException("Antrenor negasit: " + request.trainerId()));
        Room room = roomRepository
                .findById(request.roomId())
                .orElseThrow(() -> new NotFoundException("Sala negasita: " + request.roomId()));
        gc.setTrainer(trainer);
        gc.setRoom(room);
        gc.setTitle(request.title());
        gc.setStartTime(request.startTime());
        gc.setEndTime(request.endTime());
        gc.setMaxParticipants(request.maxParticipants());
        return toResponse(gymClassRepository.save(gc));
    }

    @Override
    public void delete(Long classId) {
        gymClassRepository.deleteById(load(classId).getClassId());
    }

    private void validateSchedule(GymClassRequest request) {
        if (!request.endTime().isAfter(request.startTime())) {
            throw new BadRequestException("Sfarsitul clasei trebuie sa fie dupa inceput.");
        }
    }

    private GymClass load(Long id) {
        return gymClassRepository.findById(id).orElseThrow(() -> new NotFoundException("Clasa negasita: " + id));
    }

    private GymClassResponse toResponse(GymClass gc) {
        return new GymClassResponse(
                gc.getClassId(),
                gc.getTrainer().getTrainerId(),
                gc.getRoom().getRoomId(),
                gc.getTitle(),
                gc.getStartTime(),
                gc.getEndTime(),
                gc.getMaxParticipants());
    }
}
