package org.example.vet1177.dto.response.medicalrecord;

import org.example.vet1177.entities.MedicalRecord;
import org.example.vet1177.entities.RecordStatus;

import java.time.Instant;
import java.util.UUID;

// Förenklat svar som används i listor
public record MedicalRecordSummaryResponse(
        UUID id,
        String title,
        RecordStatus status,
        String petName,
        String ownerName,
        String assignedVetName,
        Instant createdAt
) {
    public static MedicalRecordSummaryResponse from(MedicalRecord record) {
        return new MedicalRecordSummaryResponse(
                record.getId(),
                record.getTitle(),
                record.getStatus(),
                record.getPet().getName(),
                record.getOwner().getName(),
                record.getAssignedVet() != null ? record.getAssignedVet().getName() : null,
                record.getCreatedAt()
        );
    }
}
