package org.example.vet1177.dto.response.medicalrecord;

import org.example.vet1177.entities.Clinic;
import org.example.vet1177.entities.MedicalRecord;
import org.example.vet1177.entities.Pet;
import org.example.vet1177.entities.RecordStatus;
import org.example.vet1177.entities.Role;
import org.example.vet1177.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MedicalRecordResponseTest {

    private MedicalRecord record;
    private Pet pet;
    private User owner;
    private Clinic clinic;
    private User vet;
    private User createdBy;

    private UUID recordId;
    private UUID petId;
    private UUID ownerId;
    private UUID clinicId;
    private UUID vetId;
    private UUID createdById;

    @BeforeEach
    void setUp() throws Exception {
        recordId = UUID.randomUUID();
        petId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        clinicId = UUID.randomUUID();
        vetId = UUID.randomUUID();
        createdById = UUID.randomUUID();

        owner = new User("Anna Ägare", "anna@mail.se", "hash", Role.OWNER);
        setPrivateField(owner, "id", ownerId);

        vet = new User("Dr. Elin Svensson", "elin@vet.se", "hash", Role.VET);
        setPrivateField(vet, "id", vetId);

        createdBy = new User("Dr. Sara Lindqvist", "sara@vet.se", "hash", Role.VET);
        setPrivateField(createdBy, "id", createdById);

        pet = new Pet(owner, "Lassie", "Hund", "Collie", LocalDate.of(2020, 1, 1), null);
        setPrivateField(pet, "id", petId);

        clinic = new Clinic("Vetkliniken", "Storgatan 1", "0701234567");
        setPrivateField(clinic, "id", clinicId);

        record = new MedicalRecord();
        record.setId(recordId);
        record.setTitle("Årlig kontroll");
        record.setDescription("Patienten är vid god hälsa.");
        record.setStatus(RecordStatus.OPEN);
        record.setPet(pet);
        record.setOwner(owner);
        record.setClinic(clinic);
        record.setAssignedVet(vet);
        record.setCreatedBy(createdBy);
        callProtectedMethod(record, "onCreate");
    }

    // --- Happy path ---

    @Test
    void from_shouldMapAllFieldsCorrectly() {
        MedicalRecordResponse response = MedicalRecordResponse.from(record);

        assertThat(response.id()).isEqualTo(recordId);
        assertThat(response.title()).isEqualTo("Årlig kontroll");
        assertThat(response.description()).isEqualTo("Patienten är vid god hälsa.");
        assertThat(response.status()).isEqualTo(RecordStatus.OPEN);
        assertThat(response.petId()).isEqualTo(petId);
        assertThat(response.petName()).isEqualTo("Lassie");
        assertThat(response.petSpecies()).isEqualTo("Hund");
        assertThat(response.ownerId()).isEqualTo(ownerId);
        assertThat(response.ownerName()).isEqualTo("Anna Ägare");
        assertThat(response.clinicId()).isEqualTo(clinicId);
        assertThat(response.clinicName()).isEqualTo("Vetkliniken");
        assertThat(response.assignedVetId()).isEqualTo(vetId);
        assertThat(response.assignedVetName()).isEqualTo("Dr. Elin Svensson");
        assertThat(response.createdById()).isEqualTo(createdById);
        assertThat(response.createdByName()).isEqualTo("Dr. Sara Lindqvist");
        assertThat(response.createdAt()).isNotNull();
        assertThat(response.updatedAt()).isNotNull();
        assertThat(response.closedAt()).isNull();
    }

    @Test
    void from_timestampsShouldReflectRecordTimestamps() {
        MedicalRecordResponse response = MedicalRecordResponse.from(record);

        assertThat(response.createdAt()).isEqualTo(record.getCreatedAt());
        assertThat(response.updatedAt()).isEqualTo(record.getUpdatedAt());
    }

    @Test
    void from_shouldMapClosedAtWhenStatusIsClosed() {
        record.setStatus(RecordStatus.CLOSED);

        MedicalRecordResponse response = MedicalRecordResponse.from(record);

        assertThat(response.status()).isEqualTo(RecordStatus.CLOSED);
        assertThat(response.closedAt()).isEqualTo(record.getClosedAt());
    }

    @Test
    void from_shouldMapNullAssignedVetToNullIdAndName() {
        record.setAssignedVet(null);

        MedicalRecordResponse response = MedicalRecordResponse.from(record);

        assertThat(response.assignedVetId()).isNull();
        assertThat(response.assignedVetName()).isNull();
    }

    // --- Sad paths ---

    @Test
    void from_shouldThrowWhenPetIsNull() {
        record.setPet(null);

        assertThatThrownBy(() -> MedicalRecordResponse.from(record))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void from_shouldThrowWhenOwnerIsNull() {
        record.setOwner(null);

        assertThatThrownBy(() -> MedicalRecordResponse.from(record))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void from_shouldThrowWhenClinicIsNull() {
        record.setClinic(null);

        assertThatThrownBy(() -> MedicalRecordResponse.from(record))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void from_shouldThrowWhenCreatedByIsNull() {
        record.setCreatedBy(null);

        assertThatThrownBy(() -> MedicalRecordResponse.from(record))
                .isInstanceOf(NullPointerException.class);
    }

    // --- Helpers ---

    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static void callProtectedMethod(Object target, String methodName) throws Exception {
        Method method = target.getClass().getDeclaredMethod(methodName);
        method.setAccessible(true);
        method.invoke(target);
    }
}
