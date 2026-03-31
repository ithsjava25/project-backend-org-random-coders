package org.example.vet1177.dto.response.activitylog;

import org.example.vet1177.entities.ActivityLog;
import org.example.vet1177.entities.ActivityType;

import java.time.Instant;
import java.util.UUID;

public record ActivityLogResponse(

        UUID id,
        ActivityType action,
        String description,
        UUID performedById,
        String performedByName,
        UUID recordId,
        Instant createdAt

) {
    public static ActivityLogResponse from(ActivityLog log) {
        return new ActivityLogResponse(
                log.getId(),
                log.getAction(),
                log.getDescription(),
                log.getPerformedBy().getId(),
                log.getPerformedBy().getName(),
                log.getMedicalRecord().getId(),
                log.getCreatedAt()
        );
    }
}