package org.example.vet1177.policy;

import org.example.vet1177.entities.*;
import org.example.vet1177.exception.BusinessRuleException;
import org.example.vet1177.exception.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CommentPolicyTest {

    private CommentPolicy policy;

    private User admin;
    private User owner;
    private User vet;

    private Clinic clinic;
    private Clinic otherClinic;

    private MedicalRecord openRecord;
    private MedicalRecord closedRecord;

    private Comment comment;

    @BeforeEach
    void setUp() throws Exception {
        policy = new CommentPolicy();

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

        openRecord = new MedicalRecord();
        openRecord.setId(UUID.randomUUID());
        openRecord.setStatus(RecordStatus.OPEN);
        openRecord.setOwner(owner);
        openRecord.setClinic(clinic);

        closedRecord = new MedicalRecord();
        closedRecord.setId(UUID.randomUUID());
        closedRecord.setStatus(RecordStatus.CLOSED);
        closedRecord.setOwner(owner);
        closedRecord.setClinic(clinic);

        comment = new Comment();
        comment.setAuthor(owner);
        comment.setMedicalRecord(openRecord);
    }

    // -------------------------------------------------------------------------
    // canCreate
    // -------------------------------------------------------------------------

    @Test
    void canCreate_adminOnOpenRecord_shouldNotThrow() {
        assertThatNoException().isThrownBy(() -> policy.canCreate(admin, openRecord));
    }

    @Test
    void canCreate_ownerOnOwnOpenRecord_shouldNotThrow() {
        assertThatNoException().isThrownBy(() -> policy.canCreate(owner, openRecord));
    }

    @Test
    void canCreate_vetOnSameClinicOpenRecord_shouldNotThrow() {
        assertThatNoException().isThrownBy(() -> policy.canCreate(vet, openRecord));
    }

    @Test
    void canCreate_closedRecord_shouldThrowBusinessRuleException() {
        assertThatThrownBy(() -> policy.canCreate(admin, closedRecord))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Stängda ärenden kan inte kommenteras");
    }

    @Test
    void canCreate_ownerOnOthersRecord_shouldThrowForbiddenException() throws Exception {
        User otherOwner = new User("Karin Annan", "karin@mail.se", "hash", Role.OWNER);
        setPrivateField(otherOwner, "id", UUID.randomUUID());

        assertThatThrownBy(() -> policy.canCreate(otherOwner, openRecord))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Du kan inte kommentera på någon annans ärende");
    }

    @Test
    void canCreate_vetWithNullUserClinic_shouldThrowForbiddenException() throws Exception {
        User vetNoClinic = new User("Dr. Ingen Klinik", "ingen@vet.se", "hash", Role.VET);
        setPrivateField(vetNoClinic, "id", UUID.randomUUID());

        assertThatThrownBy(() -> policy.canCreate(vetNoClinic, openRecord))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Klinikuppgifter saknas");
    }

    @Test
    void canCreate_vetWithNullRecordClinic_shouldThrowForbiddenException() {
        openRecord.setClinic(null);

        assertThatThrownBy(() -> policy.canCreate(vet, openRecord))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Klinikuppgifter saknas");
    }

    @Test
    void canCreate_vetOnDifferentClinicRecord_shouldThrowForbiddenException() {
        openRecord.setClinic(otherClinic);

        assertThatThrownBy(() -> policy.canCreate(vet, openRecord))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Du kan inte kommentera ärenden på en annan klinik");
    }

    // -------------------------------------------------------------------------
    // canView
    // -------------------------------------------------------------------------

    @Test
    void canView_adminOnAnyRecord_shouldNotThrow() {
        assertThatNoException().isThrownBy(() -> policy.canView(admin, openRecord));
    }

    @Test
    void canView_ownerOnOwnRecord_shouldNotThrow() {
        assertThatNoException().isThrownBy(() -> policy.canView(owner, openRecord));
    }

    @Test
    void canView_vetOnSameClinicRecord_shouldNotThrow() {
        assertThatNoException().isThrownBy(() -> policy.canView(vet, openRecord));
    }

    @Test
    void canView_ownerOnOthersRecord_shouldThrowForbiddenException() throws Exception {
        User otherOwner = new User("Karin Annan", "karin@mail.se", "hash", Role.OWNER);
        setPrivateField(otherOwner, "id", UUID.randomUUID());

        assertThatThrownBy(() -> policy.canView(otherOwner, openRecord))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Åtkomst nekad");
    }

    @Test
    void canView_vetWithNullUserClinic_shouldThrowForbiddenException() throws Exception {
        User vetNoClinic = new User("Dr. Ingen Klinik", "ingen@vet.se", "hash", Role.VET);
        setPrivateField(vetNoClinic, "id", UUID.randomUUID());

        assertThatThrownBy(() -> policy.canView(vetNoClinic, openRecord))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Åtkomst nekad");
    }

    @Test
    void canView_vetOnDifferentClinicRecord_shouldThrowForbiddenException() {
        openRecord.setClinic(otherClinic);

        assertThatThrownBy(() -> policy.canView(vet, openRecord))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Åtkomst nekad");
    }

    // -------------------------------------------------------------------------
    // canUpdate
    // -------------------------------------------------------------------------

    @Test
    void canUpdate_authorOnOpenRecord_shouldNotThrow() {
        assertThatNoException().isThrownBy(() -> policy.canUpdate(owner, comment));
    }

    @Test
    void canUpdate_closedRecord_shouldThrowBusinessRuleException() {
        comment.setMedicalRecord(closedRecord);

        assertThatThrownBy(() -> policy.canUpdate(owner, comment))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Kommentarer på stängda ärenden kan inte redigeras");
    }

    @Test
    void canUpdate_nonAuthor_shouldThrowForbiddenException() throws Exception {
        User other = new User("Nils Annan", "nils@mail.se", "hash", Role.OWNER);
        setPrivateField(other, "id", UUID.randomUUID());

        assertThatThrownBy(() -> policy.canUpdate(other, comment))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Du kan bara redigera dina egna kommentarer");
    }

    // -------------------------------------------------------------------------
    // canDelete
    // -------------------------------------------------------------------------

    @Test
    void canDelete_adminDeletingAnyComment_shouldNotThrow() {
        assertThatNoException().isThrownBy(() -> policy.canDelete(admin, comment));
    }

    @Test
    void canDelete_authorDeletingOwnComment_shouldNotThrow() {
        assertThatNoException().isThrownBy(() -> policy.canDelete(owner, comment));
    }

    @Test
    void canDelete_nonAdminNonAuthor_shouldThrowForbiddenException() throws Exception {
        User other = new User("Nils Annan", "nils@mail.se", "hash", Role.OWNER);
        setPrivateField(other, "id", UUID.randomUUID());

        assertThatThrownBy(() -> policy.canDelete(other, comment))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Du kan bara ta bort dina egna kommentarer");
    }

    // -------------------------------------------------------------------------
    // isVisibleTo — VET_CLINICAL_NOTE döljs för OWNER
    // -------------------------------------------------------------------------

    @Test
    void isVisibleTo_ownerAndClinicalNote_shouldReturnFalse() {
        comment.setType(CommentType.VET_CLINICAL_NOTE);

        assertThat(policy.isVisibleTo(owner, comment)).isFalse();
    }

    @Test
    void isVisibleTo_ownerAndOwnerMessage_shouldReturnTrue() {
        comment.setType(CommentType.OWNER_MESSAGE);

        assertThat(policy.isVisibleTo(owner, comment)).isTrue();
    }

    @Test
    void isVisibleTo_vetAndClinicalNote_shouldReturnTrue() {
        comment.setType(CommentType.VET_CLINICAL_NOTE);

        assertThat(policy.isVisibleTo(vet, comment)).isTrue();
    }

    @Test
    void isVisibleTo_adminAndClinicalNote_shouldReturnTrue() {
        comment.setType(CommentType.VET_CLINICAL_NOTE);

        assertThat(policy.isVisibleTo(admin, comment)).isTrue();
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
