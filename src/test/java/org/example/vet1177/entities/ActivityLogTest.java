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

        //Act(simulera JPA)
        log.onCreate();

        //Assert
        assertNotNull(log.getCreatedAt());

        //extra check
        assertTrue(log.getCreatedAt().isBefore(Instant.now().plusSeconds(1)));
    }
}
