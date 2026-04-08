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

class MedicalRecordSummaryResponseTest {

    private MedicalRecord record;
    private Pet pet;
    private User owner;
    private Clinic clinic;
    private User vet;
    private User createdBy;
    private UUID recordId;

    @BeforeEach
    void setUp() throws Exception {
        recordId = UUID.randomUUID();

        owner = new User("Anna Ägare", "anna@mail.se", "hash", Role.OWNER);
        setPrivateField(owner, "id", UUID.randomUUID());

        vet = new User("Dr. Elin Svensson", "elin@vet.se", "hash", Role.VET);
        setPrivateField(vet, "id", UUID.randomUUID());

        createdBy = new User("Dr. Sara Lindqvist", "sara@vet.se", "hash", Role.VET);
        setPrivateField(createdBy, "id", UUID.randomUUID());

        pet = new Pet(owner, "Lassie", "Hund", "Collie", LocalDate.of(2020, 1, 1), null);
        setPrivateField(pet, "id", UUID.randomUUID());

        clinic = new Clinic("Vetkliniken", "Storgatan 1", "0701234567");
        setPrivateField(clinic, "id", UUID.randomUUID());

        record = new MedicalRecord();
        record.setId(recordId);
        record.setTitle("Årlig kontroll");
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
        MedicalRecordSummaryResponse response = MedicalRecordSummaryResponse.from(record);

        assertThat(response.id()).isEqualTo(recordId);
        assertThat(response.title()).isEqualTo("Årlig kontroll");
        assertThat(response.status()).isEqualTo(RecordStatus.OPEN);
        assertThat(response.petName()).isEqualTo("Lassie");
        assertThat(response.ownerName()).isEqualTo("Anna Ägare");
        assertThat(response.assignedVetName()).isEqualTo("Dr. Elin Svensson");
        assertThat(response.createdAt()).isNotNull();
    }

    @Test
    void from_createdAtShouldReflectRecordCreatedAt() {
        MedicalRecordSummaryResponse response = MedicalRecordSummaryResponse.from(record);

        assertThat(response.createdAt()).isEqualTo(record.getCreatedAt());
    }

    @Test
    void from_shouldMapNullAssignedVetToNullName() {
        record.setAssignedVet(null);

        MedicalRecordSummaryResponse response = MedicalRecordSummaryResponse.from(record);

        assertThat(response.assignedVetName()).isNull();
    }

    // --- Sad paths ---

    @Test
    void from_shouldThrowWhenPetIsNull() {
        record.setPet(null);

        assertThatThrownBy(() -> MedicalRecordSummaryResponse.from(record))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    void from_shouldThrowWhenOwnerIsNull() {
        record.setOwner(null);

        assertThatThrownBy(() -> MedicalRecordSummaryResponse.from(record))
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
