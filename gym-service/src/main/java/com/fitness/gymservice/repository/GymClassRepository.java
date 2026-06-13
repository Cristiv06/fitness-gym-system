package com.fitness.gymservice.repository;

import com.fitness.gymservice.entity.GymClass;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GymClassRepository extends JpaRepository<GymClass, Long> {
    boolean existsByTrainer_TrainerId(Long trainerId);
    boolean existsByRoom_RoomId(Long roomId);
    List<GymClass> findByTrainer_TrainerId(Long trainerId);
}
