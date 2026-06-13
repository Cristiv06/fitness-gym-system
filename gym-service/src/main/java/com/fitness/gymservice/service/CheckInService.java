package com.fitness.gymservice.service;

import com.fitness.gymservice.dto.CheckInRequest;
import com.fitness.gymservice.dto.CheckInResponse;
import java.util.List;

public interface CheckInService {
    CheckInResponse create(CheckInRequest request);
    List<CheckInResponse> findAll();
    CheckInResponse findById(Long checkinId);
    void delete(Long checkinId);
}
