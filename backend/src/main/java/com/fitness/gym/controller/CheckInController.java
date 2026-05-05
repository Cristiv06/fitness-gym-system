package com.fitness.gym.controller;

import com.fitness.gym.dto.CheckInRequest;
import com.fitness.gym.dto.CheckInResponse;
import com.fitness.gym.service.CheckInService;
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
@RequestMapping("/api/check-ins")
public class CheckInController {

    private final CheckInService service;

    public CheckInController(CheckInService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CheckInResponse create(@Valid @RequestBody CheckInRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<CheckInResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{checkinId}")
    public CheckInResponse findById(@PathVariable Long checkinId) {
        return service.findById(checkinId);
    }

    @PutMapping("/{checkinId}")
    public CheckInResponse update(@PathVariable Long checkinId, @Valid @RequestBody CheckInRequest request) {
        return service.update(checkinId, request);
    }

    @DeleteMapping("/{checkinId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long checkinId) {
        service.delete(checkinId);
    }
}
