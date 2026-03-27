package org.example.vet1177.dto.request.medicalrecord;


import jakarta.validation.constraints.NotNull;
import org.example.vet1177.entities.RecordStatus;

// Uppdatera status
public record UpdateStatusRequest(
        @NotNull(message = "Status måste anges")
        RecordStatus status
) {}