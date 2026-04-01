package org.example.vet1177.dto.response.clinic;

import org.example.vet1177.entities.Clinic;

import java.util.UUID;

public record ClinicResponse(

        UUID id,
        String name,
        String address,
        String phoneNumber

) {

    public static ClinicResponse from(Clinic clinic) {
        return new ClinicResponse(
                clinic.getId(),
                clinic.getName(),
                clinic.getAddress(),
                clinic.getPhoneNumber()
        );
    }
}