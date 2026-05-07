package com.fitness.gym.service;

import com.fitness.gym.dto.GymClassRequest;
import com.fitness.gym.dto.GymClassResponse;
import com.fitness.gym.entity.GymClass;
import com.fitness.gym.entity.Room;
import com.fitness.gym.entity.Trainer;
import com.fitness.gym.exception.BadRequestException;
import com.fitness.gym.exception.NotFoundException;
import com.fitness.gym.repository.GymClassRepository;
import com.fitness.gym.repository.RoomRepository;
import com.fitness.gym.repository.TrainerRepository;
import com.fitness.gym.service.impl.GymClassServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GymClassServiceImplTest {

    @Mock
    private GymClassRepository gymClassRepository;
    @Mock
    private TrainerRepository trainerRepository;
    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private GymClassServiceImpl service;

    private Trainer trainer;
    private Room room;
    private GymClass gymClass;
    private GymClassRequest validRequest;
    private final LocalDateTime start = LocalDateTime.of(2026, 6, 1, 10, 0);
    private final LocalDateTime end = LocalDateTime.of(2026, 6, 1, 11, 0);

    @BeforeEach
    void setUp() {
        trainer = new Trainer();
        trainer.setTrainerId(1L);
        trainer.setFullName("Test Trainer");

        room = new Room();
        room.setRoomId(1L);
        room.setName("Test Room");
        room.setMaxCapacity(20);

        gymClass = new GymClass();
        gymClass.setClassId(1L);
        gymClass.setTrainer(trainer);
        gymClass.setRoom(room);
        gymClass.setTitle("Yoga");
        gymClass.setStartTime(start);
        gymClass.setEndTime(end);
        gymClass.setMaxParticipants(20);

        validRequest = new GymClassRequest(1L, 1L, "Yoga", start, end, 20);
    }

    @Test
    void create_success() {
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(gymClassRepository.save(any(GymClass.class))).thenReturn(gymClass);

        GymClassResponse result = service.create(validRequest);

        assertThat(result.classId()).isEqualTo(1L);
        assertThat(result.title()).isEqualTo("Yoga");
        assertThat(result.trainerId()).isEqualTo(1L);
        assertThat(result.roomId()).isEqualTo(1L);
        verify(gymClassRepository).save(any(GymClass.class));
    }

    @Test
    void create_throwsBadRequest_whenEndTimeNotAfterStartTime() {
        GymClassRequest badRequest = new GymClassRequest(1L, 1L, "Yoga", end, start, 20);

        assertThatThrownBy(() -> service.create(badRequest))
                .isInstanceOf(BadRequestException.class);

        verify(gymClassRepository, never()).save(any());
    }

    @Test
    void create_throwsBadRequest_whenEndTimeEqualsStartTime() {
        GymClassRequest badRequest = new GymClassRequest(1L, 1L, "Yoga", start, start, 20);

        assertThatThrownBy(() -> service.create(badRequest))
                .isInstanceOf(BadRequestException.class);

        verify(gymClassRepository, never()).save(any());
    }

    @Test
    void create_throwsNotFound_whenTrainerNotFound() {
        when(trainerRepository.findById(99L)).thenReturn(Optional.empty());
        GymClassRequest badRequest = new GymClassRequest(99L, 1L, "Yoga", start, end, 20);

        assertThatThrownBy(() -> service.create(badRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");

        verify(gymClassRepository, never()).save(any());
    }

    @Test
    void create_throwsNotFound_whenRoomNotFound() {
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer));
        when(roomRepository.findById(99L)).thenReturn(Optional.empty());
        GymClassRequest badRequest = new GymClassRequest(1L, 99L, "Yoga", start, end, 20);

        assertThatThrownBy(() -> service.create(badRequest))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void findAll_returnsAll() {
        when(gymClassRepository.findAll()).thenReturn(List.of(gymClass));

        List<GymClassResponse> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).title()).isEqualTo("Yoga");
    }

    @Test
    void findAll_returnsEmptyList() {
        when(gymClassRepository.findAll()).thenReturn(List.of());

        List<GymClassResponse> result = service.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void findById_success() {
        when(gymClassRepository.findById(1L)).thenReturn(Optional.of(gymClass));

        GymClassResponse result = service.findById(1L);

        assertThat(result.classId()).isEqualTo(1L);
        assertThat(result.maxParticipants()).isEqualTo(20);
    }

    @Test
    void findById_throwsNotFound() {
        when(gymClassRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_success() {
        when(gymClassRepository.findById(1L)).thenReturn(Optional.of(gymClass));
        when(trainerRepository.findById(1L)).thenReturn(Optional.of(trainer));
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(gymClassRepository.save(any())).thenReturn(gymClass);

        GymClassResponse result = service.update(1L, validRequest);

        assertThat(result).isNotNull();
        verify(gymClassRepository).save(any());
    }

    @Test
    void update_throwsNotFound_whenClassNotFound() {
        when(gymClassRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, validRequest))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_success() {
        when(gymClassRepository.findById(1L)).thenReturn(Optional.of(gymClass));

        service.delete(1L);

        verify(gymClassRepository).deleteById(1L);
    }

    @Test
    void delete_throwsNotFound() {
        when(gymClassRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(NotFoundException.class);
    }
}
