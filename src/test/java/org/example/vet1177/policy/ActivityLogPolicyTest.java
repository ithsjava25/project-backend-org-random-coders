package org.example.vet1177.policy;

import org.example.vet1177.entities.*;
import org.example.vet1177.exception.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ActivityLogPolicyTest {

    private ActivityLogPolicy policy;

    private User admin;
    private User vet;
    private User owner;

    private ActivityLog log;
    private MedicalRecord record;
    private Clinic clinic;

    @BeforeEach
    void setUp() {
        policy = new ActivityLogPolicy();

        clinic = mock(Clinic.class);
        UUID clinicId = UUID.randomUUID();
        when(clinic.getId()).thenReturn(clinicId);

        // ADMIN
        admin = mock(User.class);
        when(admin.getRole()).thenReturn(Role.ADMIN);

        // VET
        vet = mock(User.class);
        when(vet.getRole()).thenReturn(Role.VET);
        when(vet.getClinic()).thenReturn(clinic);

        // OWNER
        owner = mock(User.class);
        when(owner.getRole()).thenReturn(Role.OWNER);
        UUID ownerId = UUID.randomUUID();
        when(owner.getId()).thenReturn(ownerId);

        // MedicalRecord
        record = mock(MedicalRecord.class);
        when(record.getClinic()).thenReturn(clinic);
        when(record.getOwner()).thenReturn(owner);

        // ActivityLog
        log = mock(ActivityLog.class);
        when(log.getMedicalRecord()).thenReturn(record);
    }

    // --------------------------------------------------
    // ADMIN
    // --------------------------------------------------

    @Test
    void canView_shouldAllowAdmin() {
        assertDoesNotThrow(() -> policy.canView(admin, log));
    }

    // --------------------------------------------------
    // VET
    // --------------------------------------------------

    @Test
    void canView_shouldAllowVet_sameClinic() {
        assertDoesNotThrow(() -> policy.canView(vet, log));
    }

    @Test
    void canView_shouldDenyVet_differentClinic() {
        Clinic otherClinic = mock(Clinic.class);
        when(otherClinic.getId()).thenReturn(UUID.randomUUID());
        when(vet.getClinic()).thenReturn(otherClinic);

        assertThrows(ForbiddenException.class, () ->
                policy.canView(vet, log)
        );
    }

    // --------------------------------------------------
    // OWNER
    // --------------------------------------------------

    @Test
    void canView_shouldAllowOwner_ifSameOwner() {
        assertDoesNotThrow(() -> policy.canView(owner, log));
    }

    @Test
    void canView_shouldDenyOwner_ifDifferentOwner() {
        User otherOwner = mock(User.class);
        when(otherOwner.getRole()).thenReturn(Role.OWNER);
        when(otherOwner.getId()).thenReturn(UUID.randomUUID());

        assertThrows(ForbiddenException.class, () ->
                policy.canView(otherOwner, log)
        );
    }

    // --------------------------------------------------
    // NULL CASES
    // --------------------------------------------------

    @Test
    void canView_shouldThrow_whenUserIsNull() {
        assertThrows(ForbiddenException.class, () ->
                policy.canView(null, log)
        );
    }

    @Test
    void canView_shouldThrow_whenLogIsNull() {
        assertThrows(ForbiddenException.class, () ->
                policy.canView(admin, null)
        );
    }

    @Test
    void canView_shouldThrow_whenRecordIsNull() {
        when(log.getMedicalRecord()).thenReturn(null);

        assertThrows(ForbiddenException.class, () ->
                policy.canView(admin, log)
        );
    }

    // --------------------------------------------------
    // UPDATE / DELETE
    // --------------------------------------------------

    @Test
    void canUpdate_shouldAlwaysThrow() {
        assertThrows(ForbiddenException.class, () ->
                policy.canUpdate()
        );
    }

    @Test
    void canDelete_shouldAlwaysThrow() {
        assertThrows(ForbiddenException.class, () ->
                policy.canDelete()
        );
    }
}