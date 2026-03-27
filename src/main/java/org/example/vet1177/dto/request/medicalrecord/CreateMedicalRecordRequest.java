package org.example.vet1177.dto.request.medicalrecord;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

// Skapa nytt ärende
public record CreateMedicalRecordRequest(
        @NotBlank(message = "Titel får inte vara tom")
        @Size(max = 500, message = "Titel får max vara 500 tecken")
        String title,

        @Size(max = 5000, message = "Beskrivning får max vara 5000 tecken")
        String description,

        @NotNull(message = "Djur måste anges")
        UUID petId,

        @NotNull(message = "Klinik måste anges")
        UUID clinicId

) {}