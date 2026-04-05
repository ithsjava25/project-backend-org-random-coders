package org.example.vet1177.dto.request.comment;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CreateCommentRequestValidationTest {

    private static ValidatorFactory factory;
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @AfterAll
    static void tearDown() {
        factory.close();
    }

    // --- Happy paths ---

    @Test
    void validRequest_shouldProduceNoViolations() {
        var request = new CreateCommentRequest(UUID.randomUUID(), "En giltig kommentar.");

        Set<ConstraintViolation<CreateCommentRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void bodyWithExactly5000Characters_shouldProduceNoViolations() {
        var request = new CreateCommentRequest(UUID.randomUUID(), "x".repeat(5000));

        Set<ConstraintViolation<CreateCommentRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    // --- Sad paths: recordId ---

    @Test
    void nullRecordId_shouldProduceViolationWithCorrectMessage() {
        var request = new CreateCommentRequest(null, "En giltig kommentar.");

        Set<ConstraintViolation<CreateCommentRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Ärende måste anges");
    }

    // --- Sad paths: body ---

    @Test
    void blankBody_shouldProduceViolationWithCorrectMessage() {
        var request = new CreateCommentRequest(UUID.randomUUID(), "   ");

        Set<ConstraintViolation<CreateCommentRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Kommentar får inte vara tom");
    }

    @Test
    void emptyBody_shouldProduceViolationWithCorrectMessage() {
        var request = new CreateCommentRequest(UUID.randomUUID(), "");

        Set<ConstraintViolation<CreateCommentRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Kommentar får inte vara tom");
    }

    @Test
    void nullBody_shouldProduceViolationWithCorrectMessage() {
        var request = new CreateCommentRequest(UUID.randomUUID(), null);

        Set<ConstraintViolation<CreateCommentRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Kommentar får inte vara tom");
    }

    @Test
    void bodyExceeding5000Characters_shouldProduceViolationWithCorrectMessage() {
        var request = new CreateCommentRequest(UUID.randomUUID(), "x".repeat(5001));

        Set<ConstraintViolation<CreateCommentRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Kommentar får max vara 5000 tecken");
    }

    @Test
    void allFieldsInvalid_shouldProduceTwoViolations() {
        var request = new CreateCommentRequest(null, "");

        Set<ConstraintViolation<CreateCommentRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(2);
    }
}
