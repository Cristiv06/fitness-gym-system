package com.fitness.gym.controller;

import com.fitness.gym.dto.EquipmentRequest;
import com.fitness.gym.dto.EquipmentResponse;
import com.fitness.gym.service.EquipmentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/equipment")
public class EquipmentController {

    private final EquipmentService service;

    public EquipmentController(EquipmentService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EquipmentResponse create(@Valid @RequestBody EquipmentRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<EquipmentResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{equipmentId}")
    public EquipmentResponse findById(@PathVariable Long equipmentId) {
        return service.findById(equipmentId);
    }

    @PutMapping("/{equipmentId}")
    public EquipmentResponse update(@PathVariable Long equipmentId, @Valid @RequestBody EquipmentRequest request) {
        return service.update(equipmentId, request);
    }

    @DeleteMapping("/{equipmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long equipmentId) {
        service.delete(equipmentId);
    }
}
