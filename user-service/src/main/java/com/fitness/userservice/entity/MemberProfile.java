package com.fitness.userservice.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "member_profile")
public class MemberProfile {

    @Id
    @Column(name = "member_id")
    private Long memberId;

    @MapsId
    @OneToOne(optional = false, fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "emergency_contact", length = 120)
    private String emergencyContact;

    @Column(length = 500)
    private String notes;
}
