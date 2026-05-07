package com.fitness.gym.service;

import com.fitness.gym.dto.EquipmentRequest;
import com.fitness.gym.dto.EquipmentResponse;
import com.fitness.gym.entity.Equipment;
import com.fitness.gym.exception.ConflictException;
import com.fitness.gym.exception.NotFoundException;
import com.fitness.gym.repository.EquipmentRepository;
import com.fitness.gym.service.impl.EquipmentServiceImpl;
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
class EquipmentServiceImplTest {

    @Mock
    private EquipmentRepository repository;

    @InjectMocks
    private EquipmentServiceImpl service;

    private Equipment equipment;
    private EquipmentRequest request;

    @BeforeEach
    void setUp() {
        equipment = new Equipment();
        equipment.setEquipmentId(1L);
        equipment.setName("Treadmill");

        request = new EquipmentRequest("Treadmill");
    }

    @Test
    void create_success() {
        when(repository.findByName("Treadmill")).thenReturn(Optional.empty());
        when(repository.save(any(Equipment.class))).thenReturn(equipment);

        EquipmentResponse result = service.create(request);

        assertThat(result.equipmentId()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Treadmill");
        verify(repository).save(any(Equipment.class));
    }

    @Test
    void create_throwsConflict_whenNameExists() {
        when(repository.findByName("Treadmill")).thenReturn(Optional.of(equipment));

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(ConflictException.class);

        verify(repository, never()).save(any());
    }

    @Test
    void findAll_returnsAll() {
        when(repository.findAll()).thenReturn(List.of(equipment));

        List<EquipmentResponse> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).name()).isEqualTo("Treadmill");
    }

    @Test
    void findAll_returnsEmptyList() {
        when(repository.findAll()).thenReturn(List.of());

        List<EquipmentResponse> result = service.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void findById_success() {
        when(repository.findById(1L)).thenReturn(Optional.of(equipment));

        EquipmentResponse result = service.findById(1L);

        assertThat(result.equipmentId()).isEqualTo(1L);
        assertThat(result.name()).isEqualTo("Treadmill");
    }

    @Test
    void findById_throwsNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void update_success() {
        EquipmentRequest updateRequest = new EquipmentRequest("Bike");
        when(repository.findById(1L)).thenReturn(Optional.of(equipment));
        when(repository.findByName("Bike")).thenReturn(Optional.empty());
        when(repository.save(any(Equipment.class))).thenReturn(equipment);

        EquipmentResponse result = service.update(1L, updateRequest);

        assertThat(result).isNotNull();
        verify(repository).save(any(Equipment.class));
    }

    @Test
    void update_success_sameNameSameEquipment() {
        when(repository.findById(1L)).thenReturn(Optional.of(equipment));
        when(repository.findByName("Treadmill")).thenReturn(Optional.of(equipment));
        when(repository.save(any(Equipment.class))).thenReturn(equipment);

        EquipmentResponse result = service.update(1L, request);

        assertThat(result).isNotNull();
    }

    @Test
    void update_throwsConflict_whenNameUsedByOtherEquipment() {
        Equipment other = new Equipment();
        other.setEquipmentId(2L);
        other.setName("Bike");

        when(repository.findById(1L)).thenReturn(Optional.of(equipment));
        when(repository.findByName("Bike")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> service.update(1L, new EquipmentRequest("Bike")))
                .isInstanceOf(ConflictException.class);
    }

    @Test
    void update_throwsNotFound_whenEquipmentDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, request))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void delete_success_whenNoRoomsAssigned() {
        when(repository.findById(1L)).thenReturn(Optional.of(equipment));

        service.delete(1L);

        verify(repository).delete(equipment);
    }

    @Test
    void delete_throwsNotFound_whenEquipmentDoesNotExist() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(NotFoundException.class);
    }
}
