package com.fitness.gymservice.service.impl;

import com.fitness.gymservice.dto.RoomRequest;
import com.fitness.gymservice.dto.RoomResponse;
import com.fitness.gymservice.entity.Equipment;
import com.fitness.gymservice.entity.Room;
import com.fitness.gymservice.exception.BadRequestException;
import com.fitness.gymservice.exception.NotFoundException;
import com.fitness.gymservice.repository.EquipmentRepository;
import com.fitness.gymservice.repository.GymClassRepository;
import com.fitness.gymservice.repository.RoomRepository;
import com.fitness.gymservice.service.RoomService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final EquipmentRepository equipmentRepository;
    private final GymClassRepository gymClassRepository;

    public RoomServiceImpl(RoomRepository roomRepository, EquipmentRepository equipmentRepository,
            GymClassRepository gymClassRepository) {
        this.roomRepository = roomRepository;
        this.equipmentRepository = equipmentRepository;
        this.gymClassRepository = gymClassRepository;
    }

    @Override
    public RoomResponse create(RoomRequest request) {
        Room room = new Room();
        room.setName(request.name());
        room.setMaxCapacity(request.maxCapacity());
        room.setEquipment(resolveEquipment(request.equipmentIds()));
        return toResponse(roomRepository.save(room));
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> findAll() {
        return roomRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RoomResponse findById(Long roomId) {
        return toResponse(load(roomId));
    }

    @Override
    public RoomResponse update(Long roomId, RoomRequest request) {
        Room room = load(roomId);
        room.setName(request.name());
        room.setMaxCapacity(request.maxCapacity());
        room.setEquipment(resolveEquipment(request.equipmentIds()));
        return toResponse(roomRepository.save(room));
    }

    @Override
    public void delete(Long roomId) {
        load(roomId);
        if (gymClassRepository.existsByRoom_RoomId(roomId)) {
            throw new BadRequestException("Nu se poate sterge sala: exista clase asociate.");
        }
        roomRepository.deleteById(roomId);
    }

    private Room load(Long id) {
        return roomRepository.findById(id).orElseThrow(() -> new NotFoundException("Sala negasita: " + id));
    }

    private Set<Equipment> resolveEquipment(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) return new HashSet<>();
        return ids.stream()
                .map(id -> equipmentRepository.findById(id)
                        .orElseThrow(() -> new NotFoundException("Echipament negasit: " + id)))
                .collect(Collectors.toSet());
    }

    private RoomResponse toResponse(Room r) {
        Set<Long> equipmentIds = r.getEquipment().stream().map(Equipment::getEquipmentId).collect(Collectors.toSet());
        return new RoomResponse(r.getRoomId(), r.getName(), r.getMaxCapacity(), equipmentIds);
    }
}
