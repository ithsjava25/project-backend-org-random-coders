package org.example.vet1177.policy;

import org.example.vet1177.entities.*;
import org.example.vet1177.exception.BusinessRuleException;
import org.example.vet1177.exception.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AttachmentPolicyTest {

    private AttachmentPolicy policy;

    private User admin;
    private User owner;
    private User otherOwner;
    private User vet;
    private User vetOtherClinic;

    private Clinic clinic;
    private Clinic otherClinic;

    private MedicalRecord openRecord;
    private MedicalRecord closedRecord;

    private Attachment attachmentUploadedByVet;

    private static final String JPEG = "image/jpeg";
    private static final long SMALL_FILE = 1024L;

    @BeforeEach
    void setUp() throws Exception {
        policy = new AttachmentPolicy(new MedicalRecordPolicy());

        clinic = new Clinic("Huvudkliniken", "Storgatan 1", "031-000000");
        setPrivateField(clinic, "id", UUID.randomUUID());

        otherClinic = new Clinic("Annan klinik", "Lillgatan 2", "031-111111");
        setPrivateField(otherClinic, "id", UUID.randomUUID());

        admin = new User("Admin Adminsson", "admin@vet.se", "hash", Role.ADMIN);
        setPrivateField(admin, "id", UUID.randomUUID());

        owner = new User("Anna Ägare", "anna@mail.se", "hash", Role.OWNER);
        setPrivateField(owner, "id", UUID.randomUUID());

        otherOwner = new User("Bertil Annan", "bertil@mail.se", "hash", Role.OWNER);
        setPrivateField(otherOwner, "id", UUID.randomUUID());

        vet = new User("Dr. Erik Vet", "erik@vet.se", "hash", Role.VET, clinic);
        setPrivateField(vet, "id", UUID.randomUUID());

        vetOtherClinic = new User("Dr. Sara Annan", "sara@vet.se", "hash", Role.VET, otherClinic);
        setPrivateField(vetOtherClinic, "id", UUID.randomUUID());

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

        attachmentUploadedByVet = new Attachment();
        ReflectionTestUtils.setField(attachmentUploadedByVet, "id", UUID.randomUUID());
        attachmentUploadedByVet.setMedicalRecord(openRecord);
        attachmentUploadedByVet.setUploadedBy(vet);
    }

    // -------------------------------------------------------------------------
    // canUpload
    // -------------------------------------------------------------------------

    @Test
    void canUpload_ownerOfOwnOpenRecord_shouldNotThrow() {
        assertThatNoException().isThrownBy(() ->
                policy.canUpload(owner, openRecord, JPEG, SMALL_FILE));
    }

    @Test
    void canUpload_ownerOfOwnClosedRecord_shouldThrowForbidden() {
        assertThatThrownBy(() -> policy.canUpload(owner, closedRecord, JPEG, SMALL_FILE))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Bilagor kan inte laddas upp på stängda ärenden");
    }

    @Test
    void canUpload_ownerOfOthersRecord_shouldThrowForbidden() {
        assertThatThrownBy(() -> policy.canUpload(otherOwner, openRecord, JPEG, SMALL_FILE))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Du kan bara ladda upp bilagor på egna ärenden");
    }

    @Test
    void canUpload_vetSameClinic_shouldNotThrow() {
        assertThatNoException().isThrownBy(() ->
                policy.canUpload(vet, openRecord, JPEG, SMALL_FILE));
    }

    // VET får ladda upp även på ärenden vid annan klinik (remiss/konsultflöden).
    @Test
    void canUpload_vetOtherClinic_shouldNotThrow() {
        assertThatNoException().isThrownBy(() ->
                policy.canUpload(vetOtherClinic, openRecord, JPEG, SMALL_FILE));
    }

    // VET utan klinik-koppling ska också kunna ladda upp (edge case).
    @Test
    void canUpload_vetWithoutClinic_shouldNotThrow() throws Exception {
        User vetNoClinic = new User("Dr. Lös", "los@vet.se", "hash", Role.VET);
        setPrivateField(vetNoClinic, "id", UUID.randomUUID());

        assertThatNoException().isThrownBy(() ->
                policy.canUpload(vetNoClinic, openRecord, JPEG, SMALL_FILE));
    }

    @Test
    void canUpload_admin_shouldNotThrow() {
        assertThatNoException().isThrownBy(() ->
                policy.canUpload(admin, openRecord, JPEG, SMALL_FILE));
    }

    // VET spärras nu på CLOSED (samma regel som OWNER).
    @Test
    void canUpload_vetOnClosedRecord_shouldThrowForbidden() {
        assertThatThrownBy(() -> policy.canUpload(vet, closedRecord, JPEG, SMALL_FILE))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Bilagor kan inte laddas upp på stängda ärenden");
    }

    // ADMIN får fortsatt ladda upp på stängda ärenden (retroaktiv arkivering).
    @Test
    void canUpload_adminOnClosedRecord_shouldNotThrow() {
        assertThatNoException().isThrownBy(() ->
                policy.canUpload(admin, closedRecord, JPEG, SMALL_FILE));
    }

    @Test
    void canUpload_emptyFile_shouldThrowIllegalArgument() {
        assertThatThrownBy(() -> policy.canUpload(vet, openRecord, JPEG, 0L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void canUpload_disallowedContentType_shouldThrowBusinessRule() {
        assertThatThrownBy(() -> policy.canUpload(vet, openRecord, "application/zip", SMALL_FILE))
                .isInstanceOf(BusinessRuleException.class);
    }

    // -------------------------------------------------------------------------
    // canDelete
    // -------------------------------------------------------------------------

    @Test
    void canDelete_owner_shouldThrowForbidden() {
        assertThatThrownBy(() -> policy.canDelete(owner, attachmentUploadedByVet))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Djurägare får inte radera bilagor");
    }

    @Test
    void canDelete_vetSameClinicOwnUpload_shouldNotThrow() {
        assertThatNoException().isThrownBy(() ->
                policy.canDelete(vet, attachmentUploadedByVet));
    }

    @Test
    void canDelete_vetSameClinicOthersUpload_shouldThrowForbidden() {
        User otherVet = new User("Dr. Annan Klinik", "ak@vet.se", "hash", Role.VET, clinic);
        try {
            setPrivateField(otherVet, "id", UUID.randomUUID());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        assertThatThrownBy(() -> policy.canDelete(otherVet, attachmentUploadedByVet))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void canDelete_vetOtherClinic_shouldThrowForbidden() {
        assertThatThrownBy(() -> policy.canDelete(vetOtherClinic, attachmentUploadedByVet))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Du har inte tillgång till ärenden på en annan klinik");
    }

    @Test
    void canDelete_admin_shouldNotThrow() {
        assertThatNoException().isThrownBy(() ->
                policy.canDelete(admin, attachmentUploadedByVet));
    }

    // -------------------------------------------------------------------------

    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
