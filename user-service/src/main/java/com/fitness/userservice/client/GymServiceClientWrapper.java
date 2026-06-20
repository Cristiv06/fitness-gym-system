package com.fitness.userservice.client;

import com.fitness.userservice.dto.ClassEnrollmentRequest;
import com.fitness.userservice.dto.ClassEnrollmentResponse;
import com.fitness.userservice.dto.GymClassRequest;
import com.fitness.userservice.dto.GymClassResponse;
import com.fitness.userservice.dto.TrainerResponse;
import com.fitness.userservice.dto.TrainerWithUsernameRequest;
import com.fitness.userservice.exception.ServiceUnavailableException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class GymServiceClientWrapper {

    private final GymServiceClient gymServiceClient;

    public GymServiceClientWrapper(GymServiceClient gymServiceClient) {
        this.gymServiceClient = gymServiceClient;
    }

    @CircuitBreaker(name = "gym-service", fallbackMethod = "createTrainerFallback")
    @Retry(name = "gym-service")
    public TrainerResponse createTrainerWithUsername(TrainerWithUsernameRequest request) {
        return gymServiceClient.createTrainerWithUsername(request);
    }

    public TrainerResponse createTrainerFallback(TrainerWithUsernameRequest request, Throwable t) {
        log.warn("[CIRCUIT BREAKER] gym-service indisponibil la creare trainer: {}", t.getMessage());
        throw new ServiceUnavailableException("gym-service temporar indisponibil - incearca din nou");
    }

    @CircuitBreaker(name = "gym-service", fallbackMethod = "findTrainerByUsernameFallback")
    public TrainerResponse findTrainerByUsername(String username) {
        return gymServiceClient.findTrainerByUsername(username);
    }

    public TrainerResponse findTrainerByUsernameFallback(String username, Throwable t) {
        if (t instanceof FeignException.NotFound) {
            throw (FeignException.NotFound) t;
        }
        log.warn("[CIRCUIT BREAKER] gym-service indisponibil la cautare trainer: {}", t.getMessage());
        return null;
    }

    @CircuitBreaker(name = "gym-service", fallbackMethod = "getClassesEnrolledFallback")
    public List<GymClassResponse> getClassesEnrolledByMember(Long memberId) {
        return gymServiceClient.getClassesEnrolledByMember(memberId);
    }

    public List<GymClassResponse> getClassesEnrolledFallback(Long memberId, Throwable t) {
        log.warn("[CIRCUIT BREAKER] gym-service indisponibil la clase inscrise: {}", t.getMessage());
        return List.of();
    }

    @CircuitBreaker(name = "gym-service", fallbackMethod = "getAvailableClassesFallback")
    public List<GymClassResponse> getAvailableClasses() {
        return gymServiceClient.getAvailableClasses();
    }

    public List<GymClassResponse> getAvailableClassesFallback(Throwable t) {
        log.warn("[CIRCUIT BREAKER] gym-service indisponibil la clase disponibile: {}", t.getMessage());
        return List.of();
    }

    @CircuitBreaker(name = "gym-service", fallbackMethod = "getClassesByTrainerFallback")
    public List<GymClassResponse> getClassesByTrainer(Long trainerId) {
        return gymServiceClient.getClassesByTrainer(trainerId);
    }

    public List<GymClassResponse> getClassesByTrainerFallback(Long trainerId, Throwable t) {
        log.warn("[CIRCUIT BREAKER] gym-service indisponibil la clase antrenor: {}", t.getMessage());
        return List.of();
    }

    @CircuitBreaker(name = "gym-service", fallbackMethod = "createClassFallback")
    @Retry(name = "gym-service")
    public GymClassResponse createClass(GymClassRequest request) {
        return gymServiceClient.createClass(request);
    }

    public GymClassResponse createClassFallback(GymClassRequest request, Throwable t) {
        log.warn("[CIRCUIT BREAKER] gym-service indisponibil la creare clasa: {}", t.getMessage());
        throw new ServiceUnavailableException("gym-service temporar indisponibil - incearca din nou");
    }

    @CircuitBreaker(name = "gym-service", fallbackMethod = "createEnrollmentFallback")
    @Retry(name = "gym-service")
    public ClassEnrollmentResponse createEnrollment(ClassEnrollmentRequest request) {
        return gymServiceClient.createEnrollment(request);
    }

    public ClassEnrollmentResponse createEnrollmentFallback(ClassEnrollmentRequest request, Throwable t) {
        log.warn("[CIRCUIT BREAKER] gym-service indisponibil la inscriere: {}", t.getMessage());
        throw new ServiceUnavailableException("gym-service temporar indisponibil - incearca din nou");
    }

    @CircuitBreaker(name = "gym-service", fallbackMethod = "getEnrollmentsByMemberFallback")
    public List<ClassEnrollmentResponse> getEnrollmentsByMember(Long memberId) {
        return gymServiceClient.getEnrollmentsByMember(memberId);
    }

    public List<ClassEnrollmentResponse> getEnrollmentsByMemberFallback(Long memberId, Throwable t) {
        log.warn("[CIRCUIT BREAKER] gym-service indisponibil la inscrieri: {}", t.getMessage());
        return List.of();
    }
}
