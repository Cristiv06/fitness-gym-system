package com.fitness.gym.repository;

import com.fitness.gym.entity.Trainer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    Optional<Trainer> findByUsername(String username);

    Optional<Trainer> findByEmail(String email);
}
