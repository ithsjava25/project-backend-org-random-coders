package org.example.vet1177.entities;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class VetTest {

    @Test
    void shouldCreateVetWithConstructor() {
        // Arrange
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@test.com");
        user.setPasswordHash("123");
        user.setRole(Role.VET);

        String licenseId = "LIC123";
        String specialization = "Surgery";
        String bookingInfo = "Available weekdays";

        // Act
        Vet vet = new Vet(user, licenseId, specialization, bookingInfo);

        // Assert
        assertEquals(user, vet.getUser());
        assertEquals(licenseId, vet.getLicenseId());
        assertEquals(specialization, vet.getSpecialization());
        assertEquals(bookingInfo, vet.getBookingInfo());
    }

    @Test
    void shouldSetAndGetFieldsCorrectly() {
        // Arrange
        Vet vet = new Vet();
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@test.com");
        user.setPasswordHash("123");
        user.setRole(Role.VET);

        // Act
        vet.setUser(user);
        vet.setLicenseId("LIC456");
        vet.setSpecialization("Dentistry");
        vet.setBookingInfo("Evenings only");

        // Assert
        assertEquals(user, vet.getUser());
        assertEquals("LIC456", vet.getLicenseId());
        assertEquals("Dentistry", vet.getSpecialization());
        assertEquals("Evenings only", vet.getBookingInfo());
    }

    @Test
    void shouldHaveNullFieldsWhenEmptyConstructorUsed() {
        // Act
        Vet vet = new Vet();

        // Assert
        assertNull(vet.getUser());
        assertNull(vet.getLicenseId());
        assertNull(vet.getSpecialization());
        assertNull(vet.getBookingInfo());
    }

    @Test
    void userIdShouldBeNullBeforePersist() {
        // Arrange
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@test.com");
        user.setPasswordHash("123");
        user.setRole(Role.VET);

        Vet vet = new Vet();
        vet.setUser(user);

        // Assert
        assertNull(vet.getUserId()); //  viktigt test
    }
}



//Testa:
//
//Konstruktor med alla fält sätter user, licenseId, specialization och bookingInfo korrekt
//getUserId() returnerar null innan persist
//getUser() är null när inte satt
//Setters fungerar för licenseId, specialization och bookingInfo