package com.fitness.gym.controller;

import com.fitness.gym.dto.TrainerRequest;
import com.fitness.gym.dto.TrainerResponse;
import com.fitness.gym.service.TrainerService;
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
@RequestMapping("/api/trainers")
public class TrainerController {

    private final TrainerService service;

    public TrainerController(TrainerService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrainerResponse create(@Valid @RequestBody TrainerRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<TrainerResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{trainerId}")
    public TrainerResponse findById(@PathVariable Long trainerId) {
        return service.findById(trainerId);
    }

    @PutMapping("/{trainerId}")
    public TrainerResponse update(@PathVariable Long trainerId, @Valid @RequestBody TrainerRequest request) {
        return service.update(trainerId, request);
    }

    @DeleteMapping("/{trainerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long trainerId) {
        service.delete(trainerId);
    }
}
