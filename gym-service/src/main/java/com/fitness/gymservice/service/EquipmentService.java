package com.fitness.gymservice.service;

import com.fitness.gymservice.dto.EquipmentRequest;
import com.fitness.gymservice.dto.EquipmentResponse;
import java.util.List;

public interface EquipmentService {
    EquipmentResponse create(EquipmentRequest request);
    List<EquipmentResponse> findAll();
    EquipmentResponse findById(Long equipmentId);
    EquipmentResponse update(Long equipmentId, EquipmentRequest request);
    void delete(Long equipmentId);
}
