package org.example.vet1177.dto.response.activitylog;

import org.example.vet1177.entities.ActivityLog;
import org.example.vet1177.entities.ActivityType;

import java.time.Instant;
import java.util.UUID;

public record ActivityLogResponse(

        UUID id,
        ActivityType action,
        String description,

        UUID userId,
        String userName,

        UUID recordId,

        Instant createdAt

) {

    public static ActivityLogResponse from(ActivityLog log) {
        return new ActivityLogResponse(
                log.getId(),
                log.getAction(),
                log.getDescription(),
                log.getPerformedBy() != null ? log.getPerformedBy().getId() : null,
                log.getPerformedBy() != null ? log.getPerformedBy().getName() : null,
                log.getMedicalRecord() != null ? log.getMedicalRecord().getId() : null,
                log.getCreatedAt()
        );
    }
}