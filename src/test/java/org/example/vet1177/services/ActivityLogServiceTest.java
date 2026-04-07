package org.example.vet1177.services;

import org.example.vet1177.entities.*;
import org.example.vet1177.exception.BusinessRuleException;
import org.example.vet1177.exception.ForbiddenException;
import org.example.vet1177.policy.ActivityLogPolicy;
import org.example.vet1177.repository.ActivityLogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ActivityLogServiceTest {

    private ActivityLogRepository repository;
    private ActivityLogPolicy policy;
    private ActivityLogService service;

    private User user;
    private MedicalRecord record;

    @BeforeEach
    void setUp() {
        repository = mock(ActivityLogRepository.class);
        policy = mock(ActivityLogPolicy.class);
        service = new ActivityLogService(repository, policy);

        user = mock(User.class);
        record = mock(MedicalRecord.class);
    }

    // --------------------------------------------------
    // TEST log()
    // --------------------------------------------------

    @Test
    void log_shouldSaveActivityLog_whenValidInput() {
        // Act
        service.log(ActivityType.CASE_CREATED, "Created case", user, record);

        // Assert
        verify(repository, times(1)).save(any(ActivityLog.class));
    }

    @Test
    void log_shouldThrowException_whenNullInput() {
        // Act & Assert
        assertThrows(BusinessRuleException.class, () ->
                service.log(null, "desc", user, record)
        );

        assertThrows(BusinessRuleException.class, () ->
                service.log(ActivityType.CASE_CREATED, null, user, record)
        );

        assertThrows(BusinessRuleException.class, () ->
                service.log(ActivityType.CASE_CREATED, "desc", null, record)
        );

        assertThrows(BusinessRuleException.class, () ->
                service.log(ActivityType.CASE_CREATED, "desc", user, null)
        );
    }

    // --------------------------------------------------
    // TEST getByRecord()
    // --------------------------------------------------

    @Test
    void getByRecord_shouldReturnFilteredLogs_whenUserHasAccess() {
        UUID recordId = UUID.randomUUID();

        ActivityLog log1 = mock(ActivityLog.class);
        ActivityLog log2 = mock(ActivityLog.class);

        when(repository.findByMedicalRecordIdOrderByCreatedAtDesc(recordId))
                .thenReturn(List.of(log1, log2));

        // Policy tillåter båda
        doNothing().when(policy).canView(any(), any());

        List<ActivityLog> result = service.getByRecord(recordId, user);

        assertEquals(2, result.size());
    }

    @Test
    void getByRecord_shouldFilterOutUnauthorizedLogs() {
        UUID recordId = UUID.randomUUID();

        ActivityLog allowedLog = mock(ActivityLog.class);
        ActivityLog forbiddenLog = mock(ActivityLog.class);

        when(repository.findByMedicalRecordIdOrderByCreatedAtDesc(recordId))
                .thenReturn(List.of(allowedLog, forbiddenLog));

        // Första OK
        doNothing().when(policy).canView(user, allowedLog);

        // Andra kastar exception → ska filtreras bort
        doThrow(new ForbiddenException("No access"))
                .when(policy).canView(user, forbiddenLog);

        List<ActivityLog> result = service.getByRecord(recordId, user);

        assertEquals(1, result.size());
        assertTrue(result.contains(allowedLog));
    }

    @Test
    void getByRecord_shouldThrowException_whenNullInput() {
        UUID recordId = UUID.randomUUID();

        assertThrows(ForbiddenException.class, () ->
                service.getByRecord(null, user)
        );

        assertThrows(ForbiddenException.class, () ->
                service.getByRecord(recordId, null)
        );
    }
}