package org.example.vet1177.services;

import org.example.vet1177.dto.request.vet.VetRequest;
import org.example.vet1177.dto.response.vet.VetResponse;
import org.example.vet1177.entities.*;
import org.example.vet1177.exception.BusinessRuleException;
import org.example.vet1177.exception.ResourceNotFoundException;
import org.example.vet1177.repository.UserRepository;
import org.example.vet1177.repository.VetRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VetServiceTest {

    @Mock
    private VetRepository vetRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private VetService vetService;

// =========================
// createVet
// =========================

    @Test
    void should_create_vet_successfully_and_update_role() {
        // Arrange
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setRole(Role.OWNER);

        VetRequest request = new VetRequest(
                userId,
                "LIC123",
                "Surgery",
                "Info"
        );

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(vetRepository.existsByLicenseId("LIC123")).thenReturn(false);
        when(vetRepository.existsById(userId)).thenReturn(false);
        when(vetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        // Act
        VetResponse response = vetService.createVet(request);

        // Assert
        assertNotNull(response);
        assertEquals("LIC123", response.licenseId());

        // viktig: rollen uppdateras
        assertEquals(Role.VET, user.getRole());

        verify(userRepository).save(user);
        verify(vetRepository).save(any(Vet.class));
    }

    @Test
    void should_throw_when_user_not_found() {
        UUID userId = UUID.randomUUID();

        VetRequest request = new VetRequest(userId, "LIC123", null, null);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> vetService.createVet(request));
    }

    @Test
    void should_throw_when_license_exists() {
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setRole(Role.OWNER);

        VetRequest request = new VetRequest(userId, "LIC123", null, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(vetRepository.existsByLicenseId("LIC123")).thenReturn(true);

        assertThrows(BusinessRuleException.class,
                () -> vetService.createVet(request));
    }

    @Test
    void should_throw_when_user_already_vet() {
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setRole(Role.OWNER);

        VetRequest request = new VetRequest(userId, "LIC123", null, null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(vetRepository.existsByLicenseId("LIC123")).thenReturn(false);
        when(vetRepository.existsById(userId)).thenReturn(true);

        assertThrows(BusinessRuleException.class,
                () -> vetService.createVet(request));
    }

// =========================
// getAllVets
// =========================

    @Test
    void should_return_all_vets() {
        // Arrange
        User user = new User();
        user.setName("Dr. Smith");
        user.setEmail("test@test.com");
        user.setActive(true);

        Vet vet = new Vet();
        vet.setUser(user);
        vet.setLicenseId("LIC123");

        when(vetRepository.findAll()).thenReturn(List.of(vet));

        // Act
        var result = vetService.getAllVets();

        // Assert
        assertEquals(1, result.size());
        assertEquals("Dr. Smith", result.get(0).name());

        verify(vetRepository).findAll();
    }

// =========================
// getVetById
// =========================

    @Test
    void should_return_vet_by_id() {
        UUID id = UUID.randomUUID();

        User user = new User();
        user.setName("Dr. Smith");
        user.setEmail("test@test.com");
        user.setActive(true);

        Vet vet = new Vet();
        vet.setUser(user);
        vet.setLicenseId("LIC123");

        when(vetRepository.findById(id)).thenReturn(Optional.of(vet));

        var result = vetService.getVetById(id);

        assertNotNull(result);
        assertEquals("Dr. Smith", result.name());
    }

    @Test
    void should_throw_when_vet_not_found() {
        UUID id = UUID.randomUUID();

        when(vetRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> vetService.getVetById(id));
    }


}
