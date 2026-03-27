package org.example.vet1177.dto.request.medicalrecord;

// Uppdatera titel och beskrivning
public record UpdateMedicalRecordRequest(
        String title,
        String description
) {}