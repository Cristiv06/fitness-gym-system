package com.fitness.gymservice.entity;

import jakarta.persistence.*;
import java.util.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "room")
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @Column(nullable = false, unique = true, length = 80)
    private String name;

    @Column(name = "max_capacity", nullable = false)
    private Integer maxCapacity;

    @OneToMany(mappedBy = "room")
    private List<GymClass> gymClasses = new ArrayList<>();

    @ManyToMany
    @JoinTable(name = "room_equipment",
            joinColumns = @JoinColumn(name = "room_id"),
            inverseJoinColumns = @JoinColumn(name = "equipment_id"))
    private Set<Equipment> equipment = new HashSet<>();
}
