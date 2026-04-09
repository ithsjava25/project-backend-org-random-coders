package org.example.vet1177.dto.response.vet;

import org.example.vet1177.entities.*;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class VetResponseTest {

    @Test
    void should_map_all_fields_correctly_when_clinic_exists() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();

        Clinic clinic = new Clinic();
        clinic.setName("Happy Pets Clinic");

        User user = new User();
        user.setName("Dr. Smith");
        user.setEmail("dr.smith@test.com");
        user.setActive(true);
        user.setClinic(clinic);

        // Set User ID
        Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, userId);

        Vet vet = new Vet();
        vet.setUser(user);
        vet.setLicenseId("LIC123");
        vet.setSpecialization("Surgery");
        vet.setBookingInfo("Available weekdays");

        // SÄTT userId PÅ Vet
        Field vetUserIdField = Vet.class.getDeclaredField("userId");
        vetUserIdField.setAccessible(true);
        vetUserIdField.set(vet, userId);

        // Act
        VetResponse response = VetResponse.from(vet);

        // Assert
        assertEquals(userId, response.userId());
        assertEquals("Dr. Smith", response.name());
        assertEquals("dr.smith@test.com", response.email());
        assertEquals("LIC123", response.licenseId());
        assertEquals("Surgery", response.specialization());
        assertEquals("Available weekdays", response.bookingInfo());
        assertEquals("Happy Pets Clinic", response.clinicName());
        assertTrue(response.isActive());
    }

    @Test
    void should_set_clinicName_to_null_when_clinic_is_null() throws Exception {
        // Arrange
        UUID userId = UUID.randomUUID();

        User user = new User();
        user.setName("Dr. No Clinic");
        user.setEmail("noclinic@test.com");
        user.setActive(false);
        user.setClinic(null);

        // Reflection, i tester måste man simulera JPA, @MapsId styr ID automatiskt vet.userId = user.id
        Field idField = User.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(user, userId);

        Vet vet = new Vet();
        vet.setUser(user);
        vet.setLicenseId("LIC999");
        vet.setSpecialization("Dentistry");
        vet.setBookingInfo("Weekends");

        // Act
        VetResponse response = VetResponse.from(vet);

        // Assert
        assertNull(response.clinicName());
        assertFalse(response.isActive());
    }
}