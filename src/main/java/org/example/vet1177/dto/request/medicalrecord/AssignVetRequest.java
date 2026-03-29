package org.example.vet1177.dto.request.medicalrecord;



import jakarta.validation.constraints.NotNull;

import java.util.UUID;

// Tilldela handläggare
public record AssignVetRequest(
        @NotNull(message = "Veterinär måste anges")
        UUID vetId
) {}