package com.fitness.gymservice.service.impl;

import com.fitness.gymservice.dto.EquipmentRequest;
import com.fitness.gymservice.dto.EquipmentResponse;
import com.fitness.gymservice.entity.Equipment;
import com.fitness.gymservice.exception.ConflictException;
import com.fitness.gymservice.exception.NotFoundException;
import com.fitness.gymservice.repository.EquipmentRepository;
import com.fitness.gymservice.service.EquipmentService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class EquipmentServiceImpl implements EquipmentService {

    private final EquipmentRepository equipmentRepository;

    public EquipmentServiceImpl(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    @Override
    public EquipmentResponse create(EquipmentRequest request) {
        equipmentRepository.findByName(request.name()).ifPresent(e -> {
            throw new ConflictException("Exista deja echipament cu acest nume.");
        });
        Equipment eq = new Equipment();
        eq.setName(request.name());
        return toResponse(equipmentRepository.save(eq));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EquipmentResponse> findAll() {
        return equipmentRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public EquipmentResponse findById(Long equipmentId) {
        return toResponse(load(equipmentId));
    }

    @Override
    public EquipmentResponse update(Long equipmentId, EquipmentRequest request) {
        Equipment eq = load(equipmentId);
        eq.setName(request.name());
        return toResponse(equipmentRepository.save(eq));
    }

    @Override
    public void delete(Long equipmentId) {
        equipmentRepository.deleteById(load(equipmentId).getEquipmentId());
    }

    private Equipment load(Long id) {
        return equipmentRepository.findById(id).orElseThrow(() -> new NotFoundException("Echipament negasit: " + id));
    }

    private EquipmentResponse toResponse(Equipment e) {
        return new EquipmentResponse(e.getEquipmentId(), e.getName());
    }
}
