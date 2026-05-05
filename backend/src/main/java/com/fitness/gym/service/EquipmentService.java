package com.fitness.gym.service;

import com.fitness.gym.dto.EquipmentRequest;
import com.fitness.gym.dto.EquipmentResponse;
import java.util.List;

public interface EquipmentService {

    EquipmentResponse create(EquipmentRequest request);

    List<EquipmentResponse> findAll();

    EquipmentResponse findById(Long equipmentId);

    EquipmentResponse update(Long equipmentId, EquipmentRequest request);

    void delete(Long equipmentId);
}
