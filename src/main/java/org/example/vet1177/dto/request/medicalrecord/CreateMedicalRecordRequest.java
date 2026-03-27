package org.example.vet1177.dto.request.medicalrecord;

import java.util.UUID;

// Skapa nytt ärende
public record CreateMedicalRecordRequest(
        String title,
        String description,
        UUID petId,
        UUID clinicId
) {}