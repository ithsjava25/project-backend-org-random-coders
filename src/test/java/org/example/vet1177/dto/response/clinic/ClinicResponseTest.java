package org.example.vet1177.dto.response.clinic;

import org.example.vet1177.entities.Clinic;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClinicResponseTest {

    @Test
    void shouldMapFromEntityCorrectly() {
        // Arrange
        Clinic clinic = new Clinic("Vet", "Street", "123");

        // Hack: sätt id via reflection (eller ignorera id)
        UUID id = UUID.randomUUID();

        // Act
        ClinicResponse response = ClinicResponse.from(clinic);

        // Assert
        assertEquals(clinic.getName(), response.name());
        assertEquals(clinic.getAddress(), response.address());
        assertEquals(clinic.getPhoneNumber(), response.phoneNumber());
    }
}
