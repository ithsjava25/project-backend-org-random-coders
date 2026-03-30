package org.example.vet1177.dto.response.vet;

import org.example.vet1177.entities.Vet;
import java.util.UUID;

public record VetResponse(

        UUID userId,
        String name,
        String email,
        String licenseId,
        String specialization,
        String bookingInfo,
        String clinicName,
        boolean isActive
)
{

public static VetResponse from(Vet vet) {
    return  new VetResponse(
            vet.getUserId(),
            vet.getUser().getName(),
            vet.getUser().getEmail(),
            vet.getLicenseId(),
            vet.getSpecialization(),
            vet.getBookingInfo(),
            vet.getUser().getClinic() != null ? vet.getUser().getClinic().getName() : null,
            vet.getUser().isActive()
    );
    }

}
