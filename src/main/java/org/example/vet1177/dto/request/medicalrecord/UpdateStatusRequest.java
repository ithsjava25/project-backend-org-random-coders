package org.example.vet1177.dto.request.medicalrecord;

import org.example.vet1177.entities.RecordStatus;

// Uppdatera status
public record UpdateStatusRequest(
        RecordStatus status
) {}