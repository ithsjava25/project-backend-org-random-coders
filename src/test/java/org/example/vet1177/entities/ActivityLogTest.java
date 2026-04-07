package org.example.vet1177.entities;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

public class ActivityLogTest {

    @Test
    void shouldCreateActivityLogCorrectly(){

        //Arrange
        User user = mock(User.class);
        MedicalRecord record = mock(MedicalRecord.class);

        ActivityLog log = new ActivityLog(
                ActivityType.CASE_CREATED,
                "Created case",
                user,
                record
        );

        //Assert
        assertEquals(ActivityType.CASE_CREATED, log.getAction());
        assertEquals("Created case", log.getDescription());
        assertEquals(user, log.getPerformedBy());
        assertEquals(record, log.getMedicalRecord());

    }

    @Test
    void shouldSetCreatedAtOnPrePersist(){
        //Arrange
        User user = mock(User.class);
        MedicalRecord record = mock(MedicalRecord.class);

        ActivityLog log = new ActivityLog(
                ActivityType.UPDATED,
                "Updated record",
                user,
                record
        );

        // Act (simulate JPA)
        Instant before = Instant.now();
        log.onCreate();
        Instant after = Instant.now();

        // Assert
        assertNotNull(log.getCreatedAt());
        assertTrue(!log.getCreatedAt().isBefore(before));
        assertTrue(!log.getCreatedAt().isAfter(after));
    }
}
