package com.fitness.gym.service;

import com.fitness.gym.dto.CheckInRequest;
import com.fitness.gym.dto.CheckInResponse;
import java.util.List;

public interface CheckInService {

    CheckInResponse create(CheckInRequest request);

    List<CheckInResponse> findAll();

    CheckInResponse findById(Long checkinId);

    CheckInResponse update(Long checkinId, CheckInRequest request);

    void delete(Long checkinId);
}
