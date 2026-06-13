package com.fitness.gymservice.controller;

import com.fitness.gymservice.dto.EquipmentRequest;
import com.fitness.gymservice.dto.EquipmentResponse;
import com.fitness.gymservice.service.EquipmentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EquipmentResponse create(@Valid @RequestBody EquipmentRequest request) {
        return equipmentService.create(request);
    }

    @GetMapping
    public List<EquipmentResponse> findAll() {
        return equipmentService.findAll();
    }

    @GetMapping("/{equipmentId}")
    public EquipmentResponse findById(@PathVariable Long equipmentId) {
        return equipmentService.findById(equipmentId);
    }

    @PutMapping("/{equipmentId}")
    public EquipmentResponse update(@PathVariable Long equipmentId, @Valid @RequestBody EquipmentRequest request) {
        return equipmentService.update(equipmentId, request);
    }

    @DeleteMapping("/{equipmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long equipmentId) {
        equipmentService.delete(equipmentId);
    }
}
