package com.fitness.gymservice.dto;

import java.util.Set;

public record RoomResponse(Long roomId, String name, Integer maxCapacity, Set<Long> equipmentIds) {}
