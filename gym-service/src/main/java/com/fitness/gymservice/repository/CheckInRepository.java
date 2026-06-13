package com.fitness.gymservice.repository;

import com.fitness.gymservice.entity.CheckIn;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckInRepository extends JpaRepository<CheckIn, Long> {
    List<CheckIn> findByMemberId(Long memberId);
}
