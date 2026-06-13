package com.fitness.userservice.client;

import com.fitness.userservice.dto.*;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "gym-service")
public interface GymServiceClient {

    @PostMapping("/api/trainers/with-username")
    TrainerResponse createTrainerWithUsername(@RequestBody TrainerWithUsernameRequest request);

    @GetMapping("/api/trainers/by-username/{username}")
    TrainerResponse findTrainerByUsername(@PathVariable("username") String username);

    @GetMapping("/api/gym-classes/enrolled-by-member/{memberId}")
    List<GymClassResponse> getClassesEnrolledByMember(@PathVariable("memberId") Long memberId);

    @GetMapping("/api/gym-classes/available")
    List<GymClassResponse> getAvailableClasses();

    @GetMapping("/api/gym-classes/trainer/{trainerId}")
    List<GymClassResponse> getClassesByTrainer(@PathVariable("trainerId") Long trainerId);

    @PostMapping("/api/gym-classes")
    GymClassResponse createClass(@RequestBody GymClassRequest request);

    @PostMapping("/api/class-enrollments")
    ClassEnrollmentResponse createEnrollment(@RequestBody ClassEnrollmentRequest request);

    @GetMapping("/api/class-enrollments/member/{memberId}")
    List<ClassEnrollmentResponse> getEnrollmentsByMember(@PathVariable("memberId") Long memberId);
}
