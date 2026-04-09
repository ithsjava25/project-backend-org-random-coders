package org.example.vet1177.dto.request.vet;

import jakarta.validation.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class VetRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    private void assertHasViolation(Set<ConstraintViolation<VetRequest>> violations, String field) {
        assertTrue( violations.stream()
                .anyMatch(v -> v.getPropertyPath()
                        .toString().equals(field)), "Expected violation on field: " + field ); }

    @Test
    void should_pass_validation_when_valid_input() {
        VetRequest request = new VetRequest(
                UUID.randomUUID(),
                "LIC123",
                "Surgery",
                "Available weekdays"
        );

        Set<ConstraintViolation<VetRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void should_fail_when_userId_is_null() {
        VetRequest request = new VetRequest(
                null,
                "LIC123",
                "Surgery",
                "Info"
        );

        Set<ConstraintViolation<VetRequest>> violations = validator.validate(request);

        assertHasViolation(violations, "userId");
    }

    @Test
    void should_fail_when_licenseId_is_blank() {
        VetRequest request = new VetRequest(
                UUID.randomUUID(),
                "",
                "Surgery",
                "Info"
        );

        Set<ConstraintViolation<VetRequest>> violations = validator.validate(request);

        assertHasViolation(violations, "licenseId");
    }

    @Test
    void should_fail_when_licenseId_too_long() {
        String longLicense = "A".repeat(51);

        VetRequest request = new VetRequest(
                UUID.randomUUID(),
                longLicense,
                "Surgery",
                "Info"
        );

        Set<ConstraintViolation<VetRequest>> violations = validator.validate(request);

        assertHasViolation(violations, "licenseId");
    }
}