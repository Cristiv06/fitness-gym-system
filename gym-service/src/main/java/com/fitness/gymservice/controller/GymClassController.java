package com.fitness.gymservice.controller;

import com.fitness.gymservice.dto.GymClassRequest;
import com.fitness.gymservice.dto.GymClassResponse;
import com.fitness.gymservice.service.GymClassService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/gym-classes")
public class GymClassController {

    private final GymClassService gymClassService;

    public GymClassController(GymClassService gymClassService) {
        this.gymClassService = gymClassService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public GymClassResponse create(@Valid @RequestBody GymClassRequest request) {
        return gymClassService.create(request);
    }

    @GetMapping
    public List<GymClassResponse> findAll() {
        return gymClassService.findAll();
    }

    @GetMapping("/{classId}")
    public GymClassResponse findById(@PathVariable Long classId) {
        return gymClassService.findById(classId);
    }

    @GetMapping("/trainer/{trainerId}")
    public List<GymClassResponse> findByTrainer(@PathVariable Long trainerId) {
        return gymClassService.findByTrainer(trainerId);
    }

    @GetMapping("/available")
    public List<GymClassResponse> findAvailable() {
        return gymClassService.findAvailable();
    }

    @GetMapping("/enrolled-by-member/{memberId}")
    public List<GymClassResponse> findEnrolledByMember(@PathVariable Long memberId) {
        return gymClassService.findEnrolledByMember(memberId);
    }

    @PutMapping("/{classId}")
    public GymClassResponse update(@PathVariable Long classId, @Valid @RequestBody GymClassRequest request) {
        return gymClassService.update(classId, request);
    }

    @DeleteMapping("/{classId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long classId) {
        gymClassService.delete(classId);
    }
}
