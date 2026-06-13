package com.fitness.notificationservice.client;

import com.fitness.notificationservice.dto.CheckInResponse;
import com.fitness.notificationservice.dto.GymClassResponse;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

@FeignClient(name = "gym-service")
public interface GymServiceClient {

    @GetMapping("/api/gym-classes")
    List<GymClassResponse> getAllClasses();

    @GetMapping("/api/check-ins")
    List<CheckInResponse> getAllCheckIns();
}
