package com.fitness.gymservice.controller;

import com.fitness.gymservice.dto.CheckInRequest;
import com.fitness.gymservice.dto.CheckInResponse;
import com.fitness.gymservice.service.CheckInService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/check-ins")
public class CheckInController {

    private final CheckInService checkInService;

    public CheckInController(CheckInService checkInService) {
        this.checkInService = checkInService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CheckInResponse create(@Valid @RequestBody CheckInRequest request) {
        return checkInService.create(request);
    }

    @GetMapping
    public List<CheckInResponse> findAll() {
        return checkInService.findAll();
    }

    @GetMapping("/{checkinId}")
    public CheckInResponse findById(@PathVariable Long checkinId) {
        return checkInService.findById(checkinId);
    }

    @DeleteMapping("/{checkinId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long checkinId) {
        checkInService.delete(checkinId);
    }
}
