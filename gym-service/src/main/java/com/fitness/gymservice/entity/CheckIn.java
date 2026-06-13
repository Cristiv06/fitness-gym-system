package com.fitness.gymservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "check_in")
public class CheckIn {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "checkin_id")
    private Long checkinId;

    // cross-service reference - no FK constraint to user_service_db
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "checkin_time", nullable = false)
    private LocalDateTime checkinTime;

    @PrePersist
    void prePersist() {
        if (checkinTime == null) checkinTime = LocalDateTime.now();
    }
}
