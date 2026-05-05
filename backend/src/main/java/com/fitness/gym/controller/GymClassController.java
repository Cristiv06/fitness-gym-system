package com.fitness.gym.controller;

import com.fitness.gym.dto.GymClassRequest;
import com.fitness.gym.dto.GymClassResponse;
import com.fitness.gym.service.GymClassService;
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
@RequestMapping("/api/gym-classes")
public class GymClassController {

    private final GymClassService service;

    public GymClassController(GymClassService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GymClassResponse create(@Valid @RequestBody GymClassRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<GymClassResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{classId}")
    public GymClassResponse findById(@PathVariable Long classId) {
        return service.findById(classId);
    }

    @PutMapping("/{classId}")
    public GymClassResponse update(@PathVariable Long classId, @Valid @RequestBody GymClassRequest request) {
        return service.update(classId, request);
    }

    @DeleteMapping("/{classId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long classId) {
        service.delete(classId);
    }
}
