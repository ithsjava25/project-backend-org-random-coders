package org.example.vet1177.dto.request.vet;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record VetRequest(

        @NotNull(message = "User ID får inte vara null")
        UUID userId,

        @NotBlank(message = "License-ID är obligatoriskt")
        @Size(max = 50, message = "LicensID får vara högst 50 tecken")
        String licenseId,

        @Size(max = 250, message = "Specialisering får vara max 250 tecken")
        String specialization,

        @Size(max = 500, message = "Bokningsinfo får vara högst 500 tecken")
        String bookingInfo) {

}
