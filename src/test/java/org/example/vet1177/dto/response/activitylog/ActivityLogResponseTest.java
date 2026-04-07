package org.example.vet1177.dto.response.activitylog;

import org.example.vet1177.entities.ActivityLog;
import org.example.vet1177.entities.ActivityType;
import org.example.vet1177.entities.MedicalRecord;
import org.example.vet1177.entities.User;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ActivityLogResponseTest {

    @Test
    void shouldMapActivityLogToResponseCorrectly(){
        //Arrange
        UUID logId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID recordId = UUID.randomUUID();

        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");

        User user = mock(User.class);
        MedicalRecord record = mock(MedicalRecord.class);

        when(user.getId()).thenReturn(userId);
        when(user.getName()).thenReturn("Alice");
        when(record.getId()).thenReturn(recordId);

        ActivityLog log = mock(ActivityLog.class);

        when(log.getId()).thenReturn(logId);
        when(log.getAction()).thenReturn(ActivityType.UPDATED);
        when(log.getDescription()).thenReturn("Update record");
        when(log.getPerformedBy()).thenReturn(user);
        when(log.getMedicalRecord()).thenReturn(record);
        when(log.getCreatedAt()).thenReturn(createdAt);

        //Act
        ActivityLogResponse response = ActivityLogResponse.from(log);

        //Assert
        assertEquals(logId, response.id());
        assertEquals(ActivityType.UPDATED, response.action());
        assertEquals("Update record", response.description());
        assertEquals(userId, response.performedById());
        assertEquals("Alice", response.performedByName());
        assertEquals(recordId, response.recordId());
        assertEquals(createdAt, response.createdAt());

    }


}
