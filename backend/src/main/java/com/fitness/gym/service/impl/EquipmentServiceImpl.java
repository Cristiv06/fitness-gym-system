package com.fitness.gym.service.impl;

import com.fitness.gym.dto.EquipmentRequest;
import com.fitness.gym.dto.EquipmentResponse;
import com.fitness.gym.entity.Equipment;
import com.fitness.gym.exception.ConflictException;
import com.fitness.gym.exception.NotFoundException;
import com.fitness.gym.repository.EquipmentRepository;
import com.fitness.gym.service.EquipmentService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository repository;

    public EquipmentServiceImpl(EquipmentRepository repository) {
        this.repository = repository;
    }

    @Override
    public EquipmentResponse create(EquipmentRequest request) {
        repository.findByName(request.name()).ifPresent(x -> {
            throw new ConflictException("Echipament cu acest nume exista deja.");
        });
        Equipment e = new Equipment();
        e.setName(request.name());
        return toResponse(repository.save(e));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentResponse> findAll() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EquipmentResponse findById(Long equipmentId) {
        return toResponse(load(equipmentId));
    }

    @Override
    public EquipmentResponse update(Long equipmentId, EquipmentRequest request) {
        Equipment e = load(equipmentId);
        repository.findByName(request.name())
                .filter(other -> !other.getEquipmentId().equals(equipmentId))
                .ifPresent(x -> {
                    throw new ConflictException("Alt echipament foloseste deja acest nume.");
                });
        e.setName(request.name());
        return toResponse(repository.save(e));
    }

    @Override
    public void delete(Long equipmentId) {
        Equipment e = load(equipmentId);
        if (!e.getRooms().isEmpty()) {
            e.getRooms().forEach(room -> room.getEquipment().remove(e));
            e.getRooms().clear();
        }
        repository.delete(e);
    }

    private Equipment load(Long id) {
        return repository.findById(id).orElseThrow(() -> new NotFoundException("Echipament negasit: " + id));
    }

    private EquipmentResponse toResponse(Equipment e) {
        return new EquipmentResponse(e.getEquipmentId(), e.getName());
    }
}
