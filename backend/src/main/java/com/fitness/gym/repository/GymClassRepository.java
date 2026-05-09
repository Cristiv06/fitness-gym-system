package com.fitness.gym.repository;

import com.fitness.gym.entity.GymClass;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GymClassRepository extends JpaRepository<GymClass, Long> {

    boolean existsByTrainer_TrainerId(Long trainerId);

    boolean existsByRoom_RoomId(Long roomId);

    List<GymClass> findByTrainer_Username(String username);
}
