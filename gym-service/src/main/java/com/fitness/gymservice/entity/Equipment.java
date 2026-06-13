package com.fitness.gymservice.entity;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "equipment")
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "equipment_id")
    private Long equipmentId;

    @Column(nullable = false, unique = true, length = 80)
    private String name;

    @ManyToMany(mappedBy = "equipment")
    private Set<Room> rooms = new HashSet<>();
}
