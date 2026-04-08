package org.example.vet1177.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class MedicalRecordEntityTest {

    private MedicalRecord record;
    private Pet pet;
    private User owner;
    private Clinic clinic;
    private User vet;
    private User createdBy;

    @BeforeEach
    void setUp() {
        record = new MedicalRecord();
        owner = new User("Anna Ägare", "anna@mail.se", "hash", Role.OWNER);
        vet = new User("Dr. Elin Svensson", "elin@vet.se", "hash", Role.VET);
        createdBy = new User("Dr. Sara Lindqvist", "sara@vet.se", "hash", Role.VET);
        pet = new Pet();
        clinic = new Clinic("Vetkliniken", "Storgatan 1", "0701234567");
    }

    // --- Getters & setters ---

    @Test
    void setTitle_shouldStoreAndReturnCorrectValue() {
        record.setTitle("Årlig kontroll");

        assertThat(record.getTitle()).isEqualTo("Årlig kontroll");
    }

    @Test
    void setDescription_shouldStoreAndReturnCorrectValue() {
        record.setDescription("Patienten är vid god hälsa.");

        assertThat(record.getDescription()).isEqualTo("Patienten är vid god hälsa.");
    }

    @Test
    void setPet_shouldStoreAndReturnCorrectPet() {
        record.setPet(pet);

        assertThat(record.getPet()).isSameAs(pet);
    }

    @Test
    void setOwner_shouldStoreAndReturnCorrectOwner() {
        record.setOwner(owner);

        assertThat(record.getOwner()).isSameAs(owner);
    }

    @Test
    void setClinic_shouldStoreAndReturnCorrectClinic() {
        record.setClinic(clinic);

        assertThat(record.getClinic()).isSameAs(clinic);
    }

    @Test
    void setAssignedVet_shouldStoreAndReturnCorrectVet() {
        record.setAssignedVet(vet);

        assertThat(record.getAssignedVet()).isSameAs(vet);
    }

    @Test
    void setCreatedBy_shouldStoreAndReturnCorrectUser() {
        record.setCreatedBy(createdBy);

        assertThat(record.getCreatedBy()).isSameAs(createdBy);
    }

    @Test
    void setUpdatedBy_shouldStoreAndReturnCorrectUser() {
        record.setUpdatedBy(vet);

        assertThat(record.getUpdatedBy()).isSameAs(vet);
    }

    // --- Status & closedAt logik ---

    @Test
    void defaultStatus_shouldBeOpen() {
        assertThat(record.getStatus()).isEqualTo(RecordStatus.OPEN);
    }

    @Test
    void setStatus_toClosed_shouldSetClosedAt() {
        record.setStatus(RecordStatus.CLOSED);

        assertThat(record.getStatus()).isEqualTo(RecordStatus.CLOSED);
        assertThat(record.getClosedAt()).isNotNull();
    }

    @Test
    void setStatus_fromClosedToOpen_shouldClearClosedAt() {
        record.setStatus(RecordStatus.CLOSED);
        assertThat(record.getClosedAt()).isNotNull();

        record.setStatus(RecordStatus.OPEN);

        assertThat(record.getStatus()).isEqualTo(RecordStatus.OPEN);
        assertThat(record.getClosedAt()).isNull();
    }

    @Test
    void setStatus_toClosedTwice_shouldNotOverwriteExistingClosedAt() {
        record.setStatus(RecordStatus.CLOSED);
        Instant firstClosedAt = record.getClosedAt();

        record.setStatus(RecordStatus.CLOSED);

        assertThat(record.getClosedAt()).isEqualTo(firstClosedAt);
    }

    @Test
    void setClosedAt_shouldStoreAndReturnCorrectValue() {
        Instant now = Instant.now();
        record.setClosedAt(now);

        assertThat(record.getClosedAt()).isEqualTo(now);
    }

    // --- Attachments ---

    @Test
    void getAttachments_shouldBeEmptyByDefault() {
        assertThat(record.getAttachments()).isEmpty();
    }

    @Test
    void addAttachment_shouldAddAndSetBackReference() {
        Attachment attachment = new Attachment();

        record.addAttachment(attachment);

        assertThat(record.getAttachments()).containsExactly(attachment);
        assertThat(attachment.getMedicalRecord()).isSameAs(record);
    }

    @Test
    void addAttachment_withNull_shouldBeIgnored() {
        record.addAttachment(null);

        assertThat(record.getAttachments()).isEmpty();
    }

    @Test
    void removeAttachment_shouldRemoveAndClearBackReference() {
        Attachment attachment = new Attachment();
        record.addAttachment(attachment);

        record.removeAttachment(attachment);

        assertThat(record.getAttachments()).isEmpty();
        assertThat(attachment.getMedicalRecord()).isNull();
    }

    @Test
    void removeAttachment_withNull_shouldBeIgnored() {
        Attachment attachment = new Attachment();
        record.addAttachment(attachment);

        record.removeAttachment(null);

        assertThat(record.getAttachments()).containsExactly(attachment);
    }

    @Test
    void setAttachments_shouldReplaceExistingAttachments() {
        Attachment first = new Attachment();
        Attachment second = new Attachment();
        record.addAttachment(first);

        record.setAttachments(java.util.List.of(second));

        assertThat(record.getAttachments()).containsExactly(second);
        assertThat(first.getMedicalRecord()).isNull();
        assertThat(second.getMedicalRecord()).isSameAs(record);
    }

    @Test
    void setAttachments_withNull_shouldClearExisting() {
        Attachment attachment = new Attachment();
        record.addAttachment(attachment);

        record.setAttachments(null);

        assertThat(record.getAttachments()).isEmpty();
        assertThat(attachment.getMedicalRecord()).isNull();
    }

    @Test
    void getAttachments_shouldReturnUnmodifiableList() {
        org.assertj.core.api.Assertions.assertThatThrownBy(
                () -> record.getAttachments().add(new Attachment())
        ).isInstanceOf(UnsupportedOperationException.class);
    }

    // --- Lifecycle: onCreate ---

    @Test
    void onCreate_shouldSetBothTimestampsToNonNull() {
        record.onCreate();

        assertThat(record.getCreatedAt()).isNotNull();
        assertThat(record.getUpdatedAt()).isNotNull();
    }

    // --- Lifecycle: onUpdate ---

    @Test
    void onUpdate_shouldRefreshUpdatedAtWithoutModifyingCreatedAt() {
        record.onCreate();
        Instant createdAt = record.getCreatedAt();

        record.onUpdate();

        assertThat(record.getUpdatedAt()).isNotNull();
        assertThat(record.getCreatedAt()).isEqualTo(createdAt);
    }

    @Test
    void onUpdate_shouldNotModifyCreatedAt() {
        record.onCreate();
        Instant originalCreatedAt = record.getCreatedAt();

        record.onUpdate();

        assertThat(record.getCreatedAt()).isEqualTo(originalCreatedAt);
    }

    // --- Sad paths: standardvärden innan persist ---

    @Test
    void getId_shouldBeNullBeforePersist() {
        assertThat(record.getId()).isNull();
    }

    @Test
    void getTitle_shouldBeNullWhenNotSet() {
        assertThat(record.getTitle()).isNull();
    }

    @Test
    void getDescription_shouldBeNullWhenNotSet() {
        assertThat(record.getDescription()).isNull();
    }

    @Test
    void getPet_shouldBeNullWhenNotSet() {
        assertThat(record.getPet()).isNull();
    }

    @Test
    void getOwner_shouldBeNullWhenNotSet() {
        assertThat(record.getOwner()).isNull();
    }

    @Test
    void getClinic_shouldBeNullWhenNotSet() {
        assertThat(record.getClinic()).isNull();
    }

    @Test
    void getAssignedVet_shouldBeNullWhenNotSet() {
        assertThat(record.getAssignedVet()).isNull();
    }

    @Test
    void getCreatedBy_shouldBeNullWhenNotSet() {
        assertThat(record.getCreatedBy()).isNull();
    }

    @Test
    void getUpdatedBy_shouldBeNullWhenNotSet() {
        assertThat(record.getUpdatedBy()).isNull();
    }

    @Test
    void getCreatedAt_shouldBeNullBeforeOnCreate() {
        assertThat(record.getCreatedAt()).isNull();
    }

    @Test
    void getUpdatedAt_shouldBeNullBeforeOnCreate() {
        assertThat(record.getUpdatedAt()).isNull();
    }

    @Test
    void getClosedAt_shouldBeNullByDefault() {
        assertThat(record.getClosedAt()).isNull();
    }
}
