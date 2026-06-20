package com.fitness.gymservice.client;

import com.fitness.gymservice.dto.MemberResponse;
import com.fitness.gymservice.exception.ServiceUnavailableException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserServiceClientWrapper {

    private final UserServiceClient userServiceClient;

    public UserServiceClientWrapper(UserServiceClient userServiceClient) {
        this.userServiceClient = userServiceClient;
    }

    @CircuitBreaker(name = "user-service", fallbackMethod = "getMemberByIdFallback")
    @Retry(name = "user-service")
    public MemberResponse getMemberById(Long memberId) {
        return userServiceClient.getMemberById(memberId);
    }

    public MemberResponse getMemberByIdFallback(Long memberId, Throwable t) {
        log.warn("[CIRCUIT BREAKER] user-service indisponibil, memberId={}: {}", memberId, t.getMessage());
        throw new ServiceUnavailableException("user-service temporar indisponibil - incearca din nou");
    }
}
