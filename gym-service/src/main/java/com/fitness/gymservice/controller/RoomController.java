package com.fitness.gymservice.controller;

import com.fitness.gymservice.dto.RoomRequest;
import com.fitness.gymservice.dto.RoomResponse;
import com.fitness.gymservice.service.RoomService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/rooms")
public class RoomController {

    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public RoomResponse create(@Valid @RequestBody RoomRequest request) {
        return roomService.create(request);
    }

    @GetMapping
    public List<RoomResponse> findAll() {
        return roomService.findAll();
    }

    @GetMapping("/{roomId}")
    public RoomResponse findById(@PathVariable Long roomId) {
        return roomService.findById(roomId);
    }

    @PutMapping("/{roomId}")
    public RoomResponse update(@PathVariable Long roomId, @Valid @RequestBody RoomRequest request) {
        return roomService.update(roomId, request);
    }

    @DeleteMapping("/{roomId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long roomId) {
        roomService.delete(roomId);
    }
}
