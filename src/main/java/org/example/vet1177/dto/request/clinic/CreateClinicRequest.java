package org.example.vet1177.dto.request.clinic;

import jakarta.validation.constraints.NotBlank;

public record CreateClinicRequest(

        @NotBlank(message = "Namn måste anges")
        String name,

        @NotBlank(message = "Adress måste anges")
        String address,

        @NotBlank(message = "Telefonnummer måste anges")
        String phoneNumber
) {}