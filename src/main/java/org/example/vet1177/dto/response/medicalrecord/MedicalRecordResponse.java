package org.example.vet1177.dto.response.medicalrecord;

import org.example.vet1177.entities.MedicalRecord;
import org.example.vet1177.entities.RecordStatus;

import java.time.Instant;
import java.util.UUID;

//fullständigt svar för getByID:
public record MedicalRecordResponse(
        UUID id,
        String title,
        String description,
        RecordStatus status,
        UUID petId,
        String petName,
        String petSpecies,
        UUID ownerId,
        String ownerName,
        UUID clinicId,
        String clinicName,
        UUID assignedVetId,
        String assignedVetName,
        UUID createdById,
        String createdByName,
        Instant createdAt,
        Instant updatedAt,
        Instant closedAt
) {
    public static MedicalRecordResponse from(MedicalRecord record) {
        return new MedicalRecordResponse(
                record.getId(),
                record.getTitle(),
                record.getDescription(),
                record.getStatus(),
                record.getPet().getId(),
                record.getPet().getName(),
                record.getPet().getSpecies(),
                record.getOwner().getId(),
                record.getOwner().getName(),
                record.getClinic().getId(),
                record.getClinic().getName(),
                record.getAssignedVet() != null ? record.getAssignedVet().getId() : null,
                record.getAssignedVet() != null ? record.getAssignedVet().getName() : null,
                record.getCreatedBy().getId(),
                record.getCreatedBy().getName(),
                record.getCreatedAt(),
                record.getUpdatedAt(),
                record.getClosedAt()
        );
    }
}