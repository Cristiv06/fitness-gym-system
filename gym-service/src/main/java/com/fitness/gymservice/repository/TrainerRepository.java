package com.fitness.gymservice.repository;

import com.fitness.gymservice.entity.Trainer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    Optional<Trainer> findByUsername(String username);
    Optional<Trainer> findByEmail(String email);
}
