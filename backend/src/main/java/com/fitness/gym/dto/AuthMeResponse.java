package com.fitness.gym.dto;

import java.util.List;

public record AuthMeResponse(
        String username,
        List<String> roles,
        Long memberId,
        Long trainerId
) {
}
