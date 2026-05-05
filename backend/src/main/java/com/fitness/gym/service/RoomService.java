package com.fitness.gym.service;

import com.fitness.gym.dto.RoomRequest;
import com.fitness.gym.dto.RoomResponse;
import java.util.List;

public interface RoomService {

    RoomResponse create(RoomRequest request);

    List<RoomResponse> findAll();

    RoomResponse findById(Long roomId);

    RoomResponse update(Long roomId, RoomRequest request);

    void delete(Long roomId);
}
