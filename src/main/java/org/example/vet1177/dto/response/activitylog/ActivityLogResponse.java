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
        String performedByRole,
        UUID recordId,
        String clinicName,
        String petName,
        Instant createdAt

) {
    public static ActivityLogResponse from(ActivityLog log) {
        return new ActivityLogResponse(
                log.getId(),
                log.getAction(),
                log.getDescription(),
                log.getPerformedBy().getId(),
                log.getPerformedBy().getName(),
                log.getPerformedBy().getRole() != null ? log.getPerformedBy().getRole().name() : "ROLE_UNKNOWN",
                log.getMedicalRecord().getId(),
                log.getMedicalRecord().getClinic() != null ? log.getMedicalRecord().getClinic().getName() : "Okänd klinik",
                log.getMedicalRecord().getPet() != null ? log.getMedicalRecord().getPet().getName() : "Okänt djur",
                log.getCreatedAt()
        );
    }
}