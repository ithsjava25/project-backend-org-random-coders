package org.example.vet1177.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClinicTest {

    @Test
    void shouldCreateClinicCorrectly() {
        // Arrange
        Clinic clinic = new Clinic("Vet Clinic", "Street 1", "123456");

        // Assert
        assertEquals("Vet Clinic", clinic.getName());
        assertEquals("Street 1", clinic.getAddress());
        assertEquals("123456", clinic.getPhoneNumber());
    }

    @Test
    void shouldUpdateClinicFields() {
        // Arrange
        Clinic clinic = new Clinic("Old", "Old addr", "000");

        // Act
        clinic.setName("New");
        clinic.setAddress("New addr");
        clinic.setPhoneNumber("999");

        // Assert
        assertEquals("New", clinic.getName());
        assertEquals("New addr", clinic.getAddress());
        assertEquals("999", clinic.getPhoneNumber());
    }
}
