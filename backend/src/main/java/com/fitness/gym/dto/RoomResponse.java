package com.fitness.gym.dto;

import java.util.List;

public record RoomResponse(Long roomId, String name, int maxCapacity, List<Long> equipmentIds) {
}
