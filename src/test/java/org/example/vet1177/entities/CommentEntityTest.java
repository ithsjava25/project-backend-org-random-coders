package org.example.vet1177.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class CommentEntityTest {

    private Comment comment;
    private User author;
    private MedicalRecord medicalRecord;

    @BeforeEach
    void setUp() {
        comment = new Comment();
        author = new User("Dr. Elin Svensson", "elin@vet.se", "hash123", Role.VET);
        medicalRecord = new MedicalRecord();
    }

    // --- Getters & setters ---

    @Test
    void setBody_shouldStoreAndReturnCorrectValue() {
        comment.setBody("Patienten visar tecken på förbättring.");

        assertThat(comment.getBody()).isEqualTo("Patienten visar tecken på förbättring.");
    }

    @Test
    void setAuthor_shouldStoreAndReturnCorrectUser() {
        comment.setAuthor(author);

        assertThat(comment.getAuthor()).isSameAs(author);
    }

    @Test
    void setMedicalRecord_shouldStoreAndReturnCorrectRecord() {
        comment.setMedicalRecord(medicalRecord);

        assertThat(comment.getMedicalRecord()).isSameAs(medicalRecord);
    }

    // --- Lifecycle: onCreate ---

    @Test
    void onCreate_shouldSetBothTimestampsToNonNull() {
        comment.onCreate();

        assertThat(comment.getCreatedAt()).isNotNull();
        assertThat(comment.getUpdatedAt()).isNotNull();
    }


    // --- Lifecycle: onUpdate ---

    @Test
    void onUpdate_shouldRefreshUpdatedAtWithoutModifyingCreatedAt() {
        comment.onCreate();
        Instant createdAt = comment.getCreatedAt();

        comment.onUpdate();

        assertThat(comment.getUpdatedAt()).isNotNull();
        assertThat(comment.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void onUpdate_shouldNotModifyCreatedAt() {
        comment.onCreate();
        Instant originalCreatedAt = comment.getCreatedAt();

        comment.onUpdate();

        assertThat(comment.getCreatedAt()).isEqualTo(originalCreatedAt);
    }

    // --- Sad paths: standardvärden innan persist ---

    @Test
    void getId_shouldBeNullBeforePersist() {
        assertThat(comment.getId()).isNull();
    }

    @Test
    void getBody_shouldBeNullWhenNotSet() {
        assertThat(comment.getBody()).isNull();
    }

    @Test
    void getAuthor_shouldBeNullWhenNotSet() {
        assertThat(comment.getAuthor()).isNull();
    }

    @Test
    void getMedicalRecord_shouldBeNullWhenNotSet() {
        assertThat(comment.getMedicalRecord()).isNull();
    }

    @Test
    void getCreatedAt_shouldBeNullBeforeOnCreate() {
        assertThat(comment.getCreatedAt()).isNull();
    }

    @Test
    void getUpdatedAt_shouldBeNullBeforeOnCreate() {
        assertThat(comment.getUpdatedAt()).isNull();
    }
}
