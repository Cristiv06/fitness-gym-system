package com.fitness.gym.service.impl;

import com.fitness.gym.dto.RoomRequest;
import com.fitness.gym.dto.RoomResponse;
import com.fitness.gym.entity.Equipment;
import com.fitness.gym.entity.Room;
import com.fitness.gym.exception.BadRequestException;
import com.fitness.gym.exception.ConflictException;
import com.fitness.gym.exception.NotFoundException;
import com.fitness.gym.repository.EquipmentRepository;
import com.fitness.gym.repository.GymClassRepository;
import com.fitness.gym.repository.RoomRepository;
import com.fitness.gym.service.RoomService;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final EquipmentRepository equipmentRepository;
    private final GymClassRepository gymClassRepository;

    public RoomServiceImpl(
            RoomRepository roomRepository, EquipmentRepository equipmentRepository, GymClassRepository gymClassRepository) {
        this.roomRepository = roomRepository;
        this.equipmentRepository = equipmentRepository;
        this.gymClassRepository = gymClassRepository;
    }

    @Override
    public RoomResponse create(RoomRequest request) {
        roomRepository.findByName(request.name()).ifPresent(r -> {
            throw new ConflictException("O sala cu acest nume exista deja.");
        });
        Room room = new Room();
        room.setName(request.name());
        room.setMaxCapacity(request.maxCapacity());
        syncEquipment(room, request.equipmentIds());
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
        roomRepository.findByName(request.name())
                .filter(other -> !other.getRoomId().equals(roomId))
                .ifPresent(r -> {
                    throw new ConflictException("O alta sala foloseste deja acest nume.");
                });
        room.setName(request.name());
        room.setMaxCapacity(request.maxCapacity());
        syncEquipment(room, request.equipmentIds());
        return toResponse(roomRepository.save(room));
    }

    @Override
    public void delete(Long roomId) {
        if (gymClassRepository.existsByRoom_RoomId(roomId)) {
            throw new BadRequestException("Nu se poate sterge sala: exista clase programate.");
        }
        Room room = load(roomId);
        room.getEquipment().forEach(eq -> eq.getRooms().remove(room));
        room.getEquipment().clear();
        roomRepository.delete(room);
    }

    private Room load(Long id) {
        return roomRepository.findById(id).orElseThrow(() -> new NotFoundException("Sala negasita: " + id));
    }

    private void syncEquipment(Room room, List<Long> equipmentIds) {
        if (room.getEquipment() == null) {
            room.setEquipment(new HashSet<>());
        }
        room.getEquipment().forEach(eq -> eq.getRooms().remove(room));
        room.getEquipment().clear();
        if (equipmentIds == null || equipmentIds.isEmpty()) {
            return;
        }
        Set<Equipment> resolved = new HashSet<>();
        for (Long id : equipmentIds) {
            Equipment eq = equipmentRepository
                    .findById(id)
                    .orElseThrow(() -> new NotFoundException("Echipament negasit: " + id));
            resolved.add(eq);
        }
        for (Equipment eq : resolved) {
            room.getEquipment().add(eq);
            eq.getRooms().add(room);
        }
    }

    private RoomResponse toResponse(Room room) {
        List<Long> ids = room.getEquipment() == null
                ? List.of()
                : new ArrayList<>(room.getEquipment().stream().map(Equipment::getEquipmentId).toList());
        return new RoomResponse(room.getRoomId(), room.getName(), room.getMaxCapacity(), ids);
    }
}
