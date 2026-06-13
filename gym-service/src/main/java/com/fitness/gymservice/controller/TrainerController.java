package com.fitness.gymservice.controller;

import com.fitness.gymservice.dto.TrainerRequest;
import com.fitness.gymservice.dto.TrainerResponse;
import com.fitness.gymservice.dto.TrainerWithUsernameRequest;
import com.fitness.gymservice.service.TrainerService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trainers")
public class TrainerController {

    private final TrainerService trainerService;

    public TrainerController(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TrainerResponse create(@Valid @RequestBody TrainerRequest request) {
        return trainerService.create(request);
    }

    @PostMapping("/with-username")
    @ResponseStatus(HttpStatus.CREATED)
    public TrainerResponse createWithUsername(@RequestBody TrainerWithUsernameRequest request) {
        return trainerService.createWithUsername(request);
    }

    @GetMapping
    public List<TrainerResponse> findAll() {
        return trainerService.findAll();
    }

    @GetMapping("/page")
    public Page<TrainerResponse> findAllPaged(Pageable pageable) {
        return trainerService.findAll(pageable);
    }

    @GetMapping("/{trainerId}")
    public TrainerResponse findById(@PathVariable Long trainerId) {
        return trainerService.findById(trainerId);
    }

    @GetMapping("/by-username/{username}")
    public TrainerResponse findByUsername(@PathVariable String username) {
        return trainerService.findByUsername(username);
    }

    @PutMapping("/{trainerId}")
    public TrainerResponse update(@PathVariable Long trainerId, @Valid @RequestBody TrainerRequest request) {
        return trainerService.update(trainerId, request);
    }

    @DeleteMapping("/{trainerId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long trainerId) {
        trainerService.delete(trainerId);
    }
}
