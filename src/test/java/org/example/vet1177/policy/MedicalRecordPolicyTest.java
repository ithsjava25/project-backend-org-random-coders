package org.example.vet1177.policy;

import org.example.vet1177.entities.*;
import org.example.vet1177.exception.BusinessRuleException;
import org.example.vet1177.exception.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MedicalRecordPolicyTest {

    private MedicalRecordPolicy policy;

    private User admin;
    private User owner;
    private User vet;
    private User vetOtherClinic;

    private Clinic clinic;
    private Clinic otherClinic;

    private Pet pet;

    private MedicalRecord openRecord;
    private MedicalRecord closedRecord;

    @BeforeEach
    void setUp() throws Exception {
        policy = new MedicalRecordPolicy();

        clinic = new Clinic("Huvudkliniken", "Storgatan 1", "031-000000");
        setPrivateField(clinic, "id", UUID.randomUUID());

        otherClinic = new Clinic("Annan klinik", "Lillgatan 2", "031-111111");
        setPrivateField(otherClinic, "id", UUID.randomUUID());

        admin = new User("Admin Adminsson", "admin@vet.se", "hash", Role.ADMIN);
        setPrivateField(admin, "id", UUID.randomUUID());

        owner = new User("Anna Ägare", "anna@mail.se", "hash", Role.OWNER);
        setPrivateField(owner, "id", UUID.randomUUID());

        vet = new User("Dr. Erik Vet", "erik@vet.se", "hash", Role.VET, clinic);
        setPrivateField(vet, "id", UUID.randomUUID());

        vetOtherClinic = new User("Dr. Sara Annan", "sara@vet.se", "hash", Role.VET, otherClinic);
        setPrivateField(vetOtherClinic, "id", UUID.randomUUID());

        pet = new Pet();
        pet.setOwner(owner);

        openRecord = new MedicalRecord();
        openRecord.setId(UUID.randomUUID());
        openRecord.setStatus(RecordStatus.OPEN);
        openRecord.setOwner(owner);
        openRecord.setClinic(clinic);
        openRecord.setPet(pet);

        closedRecord = new MedicalRecord();
        closedRecord.setId(UUID.randomUUID());
        closedRecord.setStatus(RecordStatus.CLOSED);
        closedRecord.setOwner(owner);
        closedRecord.setClinic(clinic);
        closedRecord.setPet(pet);
    }

    // -------------------------------------------------------------------------
    // canCreate — OWNER får inte skapa ärenden (1177-modell)
    // -------------------------------------------------------------------------

    @Test
    void canCreate_owner_shouldThrowForbidden() {
        assertThatThrownBy(() -> policy.canCreate(owner, pet, clinic))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Ägare får inte skapa ärenden");
    }

    @Test
    void canCreate_vetOnOwnClinic_shouldNotThrow() {
        assertThatNoException().isThrownBy(() -> policy.canCreate(vet, pet, clinic));
    }

    @Test
    void canCreate_vetOnOtherClinic_shouldThrowForbidden() {
        assertThatThrownBy(() -> policy.canCreate(vetOtherClinic, pet, clinic))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Du kan inte skapa ärende för en annan klinik");
    }

    @Test
    void canCreate_admin_shouldNotThrow() {
        assertThatNoException().isThrownBy(() -> policy.canCreate(admin, pet, clinic));
    }

    // -------------------------------------------------------------------------
    // canUpdate — OWNER får inte uppdatera ärenden
    // -------------------------------------------------------------------------

    @Test
    void canUpdate_owner_shouldThrowForbidden() {
        assertThatThrownBy(() -> policy.canUpdate(owner, openRecord))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Ägare får inte uppdatera ärenden");
    }

    @Test
    void canUpdate_vetOnOwnClinic_shouldNotThrow() {
        assertThatNoException().isThrownBy(() -> policy.canUpdate(vet, openRecord));
    }

    @Test
    void canUpdate_vetOnOtherClinic_shouldThrowForbidden() {
        assertThatThrownBy(() -> policy.canUpdate(vetOtherClinic, openRecord))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Du har inte tillgång till ärenden på en annan klinik");
    }

    @Test
    void canUpdate_admin_shouldNotThrow() {
        assertThatNoException().isThrownBy(() -> policy.canUpdate(admin, openRecord));
    }

    @Test
    void canUpdate_closedRecord_shouldThrowBusinessRule() {
        assertThatThrownBy(() -> policy.canUpdate(vet, closedRecord))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Stängda ärenden kan inte uppdateras");
    }

    // -------------------------------------------------------------------------
    // canView — OWNER får se egna, VET på samma klinik, ADMIN alltid
    // -------------------------------------------------------------------------

    @Test
    void canView_ownerOfRecord_shouldNotThrow() {
        assertThatNoException().isThrownBy(() -> policy.canView(owner, openRecord));
    }

    @Test
    void canView_otherOwner_shouldThrowForbidden() throws Exception {
        User otherOwner = new User("Bertil Annan", "b@mail.se", "hash", Role.OWNER);
        setPrivateField(otherOwner, "id", UUID.randomUUID());

        assertThatThrownBy(() -> policy.canView(otherOwner, openRecord))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void canView_vetSameClinic_shouldNotThrow() {
        assertThatNoException().isThrownBy(() -> policy.canView(vet, openRecord));
    }

    @Test
    void canView_vetOtherClinic_shouldThrowForbidden() {
        assertThatThrownBy(() -> policy.canView(vetOtherClinic, openRecord))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void canView_admin_shouldNotThrow() {
        assertThatNoException().isThrownBy(() -> policy.canView(admin, openRecord));
    }

    // -------------------------------------------------------------------------
    // canClose / canUpdateStatus / canAssignVet — OWNER blockeras fortsatt
    // -------------------------------------------------------------------------

    @Test
    void canClose_owner_shouldThrowForbidden() {
        assertThatThrownBy(() -> policy.canClose(owner, openRecord))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void canUpdateStatus_owner_shouldThrowForbidden() {
        assertThatThrownBy(() -> policy.canUpdateStatus(owner, openRecord, RecordStatus.IN_PROGRESS))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void canAssignVet_owner_shouldThrowForbidden() {
        assertThatThrownBy(() -> policy.canAssignVet(owner, openRecord, vet))
                .isInstanceOf(ForbiddenException.class);
    }

    // -------------------------------------------------------------------------
    // canViewClinic — OWNER blockeras, VET på rätt klinik släpps, ADMIN alltid
    // -------------------------------------------------------------------------

    @Test
    void canViewClinic_owner_shouldThrowForbidden() {
        assertThatThrownBy(() -> policy.canViewClinic(owner, clinic.getId()))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void canViewClinic_vetOwnClinic_shouldNotThrow() {
        assertThatNoException().isThrownBy(() -> policy.canViewClinic(vet, clinic.getId()));
    }

    @Test
    void canViewClinic_vetOtherClinic_shouldThrowForbidden() {
        assertThatThrownBy(() -> policy.canViewClinic(vetOtherClinic, clinic.getId()))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void canViewClinic_admin_shouldNotThrow() {
        assertThatNoException().isThrownBy(() -> policy.canViewClinic(admin, clinic.getId()));
    }

    // -------------------------------------------------------------------------

    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
