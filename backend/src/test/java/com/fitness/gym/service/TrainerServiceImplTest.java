package com.fitness.gym.service;

import com.fitness.gym.dto.TrainerRequest;
import com.fitness.gym.dto.TrainerResponse;
import com.fitness.gym.entity.Trainer;
import com.fitness.gym.exception.BadRequestException;
import com.fitness.gym.exception.NotFoundException;
import com.fitness.gym.repository.GymClassRepository;
import com.fitness.gym.repository.TrainerRepository;
import com.fitness.gym.service.impl.TrainerServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainerServiceImplTest {

    @Mock
    private TrainerRepository repository;
    @Mock
    private GymClassRepository gymClassRepository;

    @InjectMocks
    private TrainerServiceImpl service;

    private Trainer trainer;
    private TrainerRequest request;

    @BeforeEach
    void setUp() {
        trainer = new Trainer();
        trainer.setTrainerId(1L);
        trainer.setFullName("Ana Pop");
        trainer.setSpecialization("Yoga");
        trainer.setPhone("0700111222");
        trainer.setEmail("ana@gym.ro");

        request = new TrainerRequest("Ana Pop", "Yoga", "0700111222", "ana@gym.ro");
    }

    @Test
    void create_success() {
        when(repository.save(any(Trainer.class))).thenReturn(trainer);

        TrainerResponse result = service.create(request);

        assertThat(result.trainerId()).isEqualTo(1L);
        assertThat(result.fullName()).isEqualTo("Ana Pop");
        assertThat(result.specialization()).isEqualTo("Yoga");
        verify(repository).save(any(Trainer.class));
    }

    @Test
    void create_withNullOptionalFields() {
        TrainerRequest minimalRequest = new TrainerRequest("Minimal Trainer", null, null, null);
        Trainer minimal = new Trainer();
        minimal.setTrainerId(2L);
        minimal.setFullName("Minimal Trainer");
        when(repository.save(any(Trainer.class))).thenReturn(minimal);

        TrainerResponse result = service.create(minimalRequest);

        assertThat(result.fullName()).isEqualTo("Minimal Trainer");
    }

    @Test
    void findAll_returnsAll() {
        when(repository.findAll()).thenReturn(List.of(trainer));

        List<TrainerResponse> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).email()).isEqualTo("ana@gym.ro");
    }

    @Test
    void findAll_returnsEmptyList() {
        when(repository.findAll()).thenReturn(List.of());

        List<TrainerResponse> result = service.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void findById_success() {
        when(repository.findById(1L)).thenReturn(Optional.of(trainer));

        TrainerResponse result = service.findById(1L);

        assertThat(result.trainerId()).isEqualTo(1L);
        assertThat(result.phone()).isEqualTo("0700111222");
    }

    @Test
    void findById_throwsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_success() {
        TrainerRequest updateRequest = new TrainerRequest("Ana Maria Pop", "Pilates", "0700999888", "anamaria@gym.ro");
        when(repository.findById(1L)).thenReturn(Optional.of(trainer));
        when(repository.save(any(Trainer.class))).thenReturn(trainer);

        TrainerResponse result = service.update(1L, updateRequest);

        assertThat(result).isNotNull();
        verify(repository).save(any(Trainer.class));
    }

    @Test
    void update_throwsNotFound_whenTrainerDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_success() {
        when(repository.findById(1L)).thenReturn(Optional.of(trainer));
        when(gymClassRepository.existsByTrainer_TrainerId(1L)).thenReturn(false);

        service.delete(1L);

        verify(repository).deleteById(1L);
    }

    @Test
    void delete_throwsBadRequest_whenClassesExist() {
        when(repository.findById(1L)).thenReturn(Optional.of(trainer));
        when(gymClassRepository.existsByTrainer_TrainerId(1L)).thenReturn(true);

        assertThatThrownBy(() -> service.delete(1L))
                .isInstanceOf(BadRequestException.class);

        verify(repository, never()).deleteById(any());
    }

    @Test
    void delete_throwsNotFound_whenTrainerDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(NotFoundException.class);
    }
}
