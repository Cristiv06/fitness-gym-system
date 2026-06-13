package com.fitness.gymservice.controller;

import com.fitness.gymservice.dto.ClassEnrollmentRequest;
import com.fitness.gymservice.dto.ClassEnrollmentResponse;
import com.fitness.gymservice.service.ClassEnrollmentService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/class-enrollments")
public class ClassEnrollmentController {

    private final ClassEnrollmentService enrollmentService;

    public ClassEnrollmentController(ClassEnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ClassEnrollmentResponse create(@Valid @RequestBody ClassEnrollmentRequest request) {
        return enrollmentService.create(request);
    }

    @GetMapping
    public List<ClassEnrollmentResponse> findAll() {
        return enrollmentService.findAll();
    }

    @GetMapping("/{enrollmentId}")
    public ClassEnrollmentResponse findById(@PathVariable Long enrollmentId) {
        return enrollmentService.findById(enrollmentId);
    }

    @GetMapping("/member/{memberId}")
    public List<ClassEnrollmentResponse> findByMember(@PathVariable Long memberId) {
        return enrollmentService.findByMember(memberId);
    }

    @PutMapping("/{enrollmentId}")
    public ClassEnrollmentResponse update(@PathVariable Long enrollmentId,
            @Valid @RequestBody ClassEnrollmentRequest request) {
        return enrollmentService.update(enrollmentId, request);
    }

    @DeleteMapping("/{enrollmentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long enrollmentId) {
        enrollmentService.delete(enrollmentId);
    }
}
