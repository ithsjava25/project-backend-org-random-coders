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

        setUserId(user, userId);

        Vet vet = new Vet();
        vet.setUser(user);
        vet.setLicenseId("LIC123");
        vet.setSpecialization("Surgery");
        vet.setBookingInfo("Available weekdays");

        setVetUserId(vet, userId);

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

        setUserId(user, userId);

        Vet vet = new Vet();
        vet.setUser(user);
        vet.setLicenseId("LIC999");
        vet.setSpecialization("Dentistry");
        vet.setBookingInfo("Weekends");

        setVetUserId(vet, userId);

        // Act
        VetResponse response = VetResponse.from(vet);

        // Assert
        assertEquals(userId, response.userId());
        assertEquals("Dr. No Clinic", response.name());
        assertEquals("noclinic@test.com", response.email());
        assertEquals("LIC999", response.licenseId());
        assertEquals("Dentistry", response.specialization());
        assertEquals("Weekends", response.bookingInfo());
        assertNull(response.clinicName());
        assertFalse(response.isActive());
    }

// Helper methods (gör testen renare och mer professionella)

    private void setUserId(User user, UUID id) throws Exception {
        Field field = User.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(user, id);
    }

    private void setVetUserId(Vet vet, UUID id) throws Exception {
        Field field = Vet.class.getDeclaredField("userId");
        field.setAccessible(true);
        field.set(vet, id);
    }

}
