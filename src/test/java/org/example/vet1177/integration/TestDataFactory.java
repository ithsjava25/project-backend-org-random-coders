package org.example.vet1177.integration;

import org.example.vet1177.entities.*;
import org.example.vet1177.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public class TestDataFactory {

    public static Clinic createClinic(ClinicRepository clinicRepository) {
        Clinic clinic = new Clinic();
        clinic.setName("Test Clinic " + UUID.randomUUID());
        return clinicRepository.save(clinic);
    }

    public static User createOwner(UserRepository userRepository, Clinic clinic) {
        User owner = new User(
                "Owner",
                UUID.randomUUID() + "@test.com",
                "password123",
                Role.OWNER,
                clinic
        );
        return userRepository.save(owner);
    }

    public static Pet createPet(PetRepository petRepository, User owner) {
        Pet pet = new Pet(
                owner,
                "Doggo",
                "Dog",
                "Labrador",
                LocalDate.of(2020, 1, 1),
                new BigDecimal("20.5")
        );
        return petRepository.save(pet);
    }

    public static MedicalRecord createRecord(
            MedicalRecordRepository medicalRecordRepository,
            User owner,
            Clinic clinic,
            Pet pet
    ) {
        MedicalRecord record = new MedicalRecord();
        record.setTitle("Test Record");
        record.setOwner(owner);
        record.setClinic(clinic);
        record.setCreatedBy(owner);
        record.setPet(pet);

        return medicalRecordRepository.save(record);
    }

    public static ActivityLog createLog(
            ActivityLogRepository repo,
            User user,
            MedicalRecord record,
            ActivityType type,
            String desc
    ) {
        ActivityLog log = new ActivityLog(type, desc, user, record);
        return repo.save(log);
    }
}
