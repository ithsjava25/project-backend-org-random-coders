package org.example.vet1177.services;

import org.example.vet1177.entities.Clinic;
import org.example.vet1177.exception.ResourceNotFoundException;
import org.example.vet1177.repository.ClinicRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ClinicServiceTest {

    private final ClinicRepository repository = mock(ClinicRepository.class);
    private final ClinicService service = new ClinicService(repository);

    @Test
    void shouldCreateClinic() {
        // Arrange
        Clinic clinic = new Clinic("Vet", "Street", "123");
        when(repository.save(any())).thenReturn(clinic);

        // Act
        Clinic result = service.create("Vet", "Street", "123");

        // Assert
        assertEquals("Vet", result.getName());
        verify(repository).save(any());
    }

    @Test
    void shouldGetClinicById() {
        // Arrange
        UUID id = UUID.randomUUID();
        Clinic clinic = new Clinic("Vet", "Street", "123");

        when(repository.findById(id)).thenReturn(Optional.of(clinic));

        // Act
        Clinic result = service.getById(id);

        // Assert
        assertEquals(clinic, result);
    }

    @Test
    void shouldThrowWhenClinicNotFound() {
        // Arrange
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        // Act + Assert
        assertThrows(ResourceNotFoundException.class, () -> service.getById(id));
    }

    @Test
    void shouldUpdateClinic() {
        // Arrange
        UUID id = UUID.randomUUID();
        Clinic clinic = new Clinic("Old", "Old", "000");

        when(repository.findById(id)).thenReturn(Optional.of(clinic));
        when(repository.save(any())).thenReturn(clinic);

        // Act
        Clinic updated = service.update(id, "New", "New", "999");

        // Assert
        assertEquals("New", updated.getName());
        verify(repository).save(clinic);
    }

    @Test
    void shouldDeleteClinic() {
        // Arrange
        UUID id = UUID.randomUUID();
        Clinic clinic = new Clinic("Vet", "Street", "123");

        when(repository.findById(id)).thenReturn(Optional.of(clinic));

        // Act
        service.delete(id);

        // Assert
        verify(repository).delete(clinic);
    }
}
