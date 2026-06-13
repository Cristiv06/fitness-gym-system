package com.fitness.gymservice.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "class_enrollment",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "class_id"}))
public class ClassEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "enrollment_id")
    private Long enrollmentId;

    // cross-service reference - no FK constraint to user_service_db
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @ManyToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id", nullable = false)
    private GymClass gymClass;

    @Column(name = "enrolled_at", nullable = false)
    private LocalDateTime enrolledAt;

    @PrePersist
    void prePersist() {
        if (enrolledAt == null) enrolledAt = LocalDateTime.now();
    }
}
