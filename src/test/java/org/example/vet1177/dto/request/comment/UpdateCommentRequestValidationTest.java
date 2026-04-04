package org.example.vet1177.dto.request.comment;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class UpdateCommentRequestValidationTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    // --- Happy paths ---

    @Test
    void validRequest_shouldProduceNoViolations() {
        var request = new UpdateCommentRequest("Uppdaterad kommentar med ny information.");

        Set<ConstraintViolation<UpdateCommentRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    @Test
    void bodyWithExactly5000Characters_shouldProduceNoViolations() {
        var request = new UpdateCommentRequest("x".repeat(5000));

        Set<ConstraintViolation<UpdateCommentRequest>> violations = validator.validate(request);

        assertThat(violations).isEmpty();
    }

    // --- Sad paths: body ---

    @Test
    void blankBody_shouldProduceViolationWithCorrectMessage() {
        var request = new UpdateCommentRequest("   ");

        Set<ConstraintViolation<UpdateCommentRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Kommentar får inte vara tom");
    }

    @Test
    void emptyBody_shouldProduceViolationWithCorrectMessage() {
        var request = new UpdateCommentRequest("");

        Set<ConstraintViolation<UpdateCommentRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Kommentar får inte vara tom");
    }

    @Test
    void nullBody_shouldProduceViolationWithCorrectMessage() {
        var request = new UpdateCommentRequest(null);

        Set<ConstraintViolation<UpdateCommentRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Kommentar får inte vara tom");
    }

    @Test
    void bodyExceeding5000Characters_shouldProduceViolationWithCorrectMessage() {
        var request = new UpdateCommentRequest("x".repeat(5001));

        Set<ConstraintViolation<UpdateCommentRequest>> violations = validator.validate(request);

        assertThat(violations).hasSize(1);
        assertThat(violations.iterator().next().getMessage()).isEqualTo("Kommentar får max vara 5000 tecken");
    }
}
