package com.fitness.gym.controller;

import com.fitness.gym.dto.ClassEnrollmentRequest;
import com.fitness.gym.dto.ClassEnrollmentResponse;
import com.fitness.gym.service.ClassEnrollmentService;
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
@RequestMapping("/api/class-enrollments")
public class ClassEnrollmentController {

    private final ClassEnrollmentService service;

    public ClassEnrollmentController(ClassEnrollmentService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClassEnrollmentResponse create(@Valid @RequestBody ClassEnrollmentRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<ClassEnrollmentResponse> findAll() {
        return service.findAll();
    }

    @GetMapping("/{enrollmentId}")
    public ClassEnrollmentResponse findById(@PathVariable Long enrollmentId) {
        return service.findById(enrollmentId);
    }

    @PutMapping("/{enrollmentId}")
    public ClassEnrollmentResponse update(
            @PathVariable Long enrollmentId, @Valid @RequestBody ClassEnrollmentRequest request) {
        return service.update(enrollmentId, request);
    }

    @DeleteMapping("/{enrollmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long enrollmentId) {
        service.delete(enrollmentId);
    }
}
