package org.example.vet1177.dto.request.medicalrecord;

import java.util.UUID;

// Tilldela handläggare
public record AssignVetRequest(
        UUID vetId
) {}