package org.example.vet1177.dto.request.medicalrecord;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Uppdatera titel och beskrivning
public record UpdateMedicalRecordRequest(
        @NotBlank(message = "Titel får inte vara tom")
        @Size(max = 500, message = "Titel får max vara 500 tecken")
        String title,

        @Size(max = 5000, message = "Beskrivning får max vara 5000 tecken")
        String description
) {}