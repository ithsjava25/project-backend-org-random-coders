package org.example.vet1177.services;

import org.example.vet1177.entities.*;
import org.example.vet1177.exception.BusinessRuleException;
import org.example.vet1177.exception.ResourceNotFoundException;
import org.example.vet1177.policy.MedicalRecordPolicy;
import org.example.vet1177.repository.ClinicRepository;
import org.example.vet1177.repository.MedicalRecordRepository;
import org.example.vet1177.repository.PetRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;
    private final PetRepository petRepository;
    private final ClinicRepository clinicRepository;
    private final MedicalRecordPolicy medicalRecordPolicy;
    private final ActivityLogService activityLogService;

    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository,
                                PetRepository petRepository,
                                ClinicRepository clinicRepository,
                                MedicalRecordPolicy medicalRecordPolicy,
                                ActivityLogService activityLogService) {
        this.medicalRecordRepository = medicalRecordRepository;
        this.petRepository = petRepository;
        this.clinicRepository = clinicRepository;
        this.medicalRecordPolicy = medicalRecordPolicy;
        this.activityLogService = activityLogService;
    }

    // ── Skapa ────────────────────────────────────────────────

    public MedicalRecord create(
            String title,
            String description,
            UUID petId,
            UUID clinicId,
            User currentUser) {

        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet", petId));

        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic", clinicId));

        medicalRecordPolicy.canCreate(currentUser, pet, clinic);

        MedicalRecord record = new MedicalRecord();
        record.setTitle(title);
        record.setDescription(description);
        record.setPet(pet);
        record.setOwner(pet.getOwner());  // ← hämtas från pet
        record.setClinic(clinic);
        record.setCreatedBy(currentUser);
        record.setStatus(RecordStatus.OPEN);

//        return medicalRecordRepository.save(record);
        MedicalRecord saved = medicalRecordRepository.save(record);

        // LOGGING
        activityLogService.log(
                ActivityType.CASE_CREATED,
                "Ärende skapat",
                currentUser,
                saved
        );

        return saved;
    }

    // ── Läsa ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public MedicalRecord getById(UUID id) {
        return medicalRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("MedicalRecord", id));  // ← rad 49
    }

    @Transactional(readOnly = true)
    public List<MedicalRecord> getByPet(UUID petId) {
        return medicalRecordRepository.findByPetId(petId);
    }

    @Transactional(readOnly = true)
    public List<MedicalRecord> getByOwner(UUID ownerId) {
        return medicalRecordRepository.findByOwnerId(ownerId);
    }

    @Transactional(readOnly = true)
    public List<MedicalRecord> getByClinic(UUID clinicId) {
        return medicalRecordRepository.findByClinicId(clinicId);
    }

    @Transactional(readOnly = true)
    public List<MedicalRecord> getByClinicAndStatus(UUID clinicId, RecordStatus status) {
        return medicalRecordRepository.findByClinicIdAndStatus(clinicId, status);
    }

    @Transactional(readOnly = true)
    public List<MedicalRecord> getByPetAllowedForUser(UUID petId, User currentUser) {
        List<MedicalRecord> all = medicalRecordRepository.findByPetId(petId);

        return all.stream()
                .filter(record -> medicalRecordPolicy.isAllowed(currentUser, record))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MedicalRecord> getByOwnerAllowedForUser(UUID ownerId, User currentUser) {
        List<MedicalRecord> all = medicalRecordRepository.findByOwnerId(ownerId);

        return all.stream()
                .filter(record -> medicalRecordPolicy.isAllowed(currentUser, record))
                .toList();
    }

    // ── Uppdatera ─────────────────────────────────────────────

    public MedicalRecord update(UUID id, String title, String description, User updatedBy) {
        MedicalRecord record = getById(id);

        if (record.getStatus().isFinal()) {
            throw new BusinessRuleException("Stängda ärenden kan inte uppdateras");
        }

        record.setTitle(title);
        record.setDescription(description);
        record.setUpdatedBy(updatedBy);
//        return medicalRecordRepository.save(record);

        MedicalRecord updated = medicalRecordRepository.save(record);

        // LOGGING
        activityLogService.log(
                ActivityType.UPDATED,
                "Ärende uppdaterat",
                updatedBy,
                updated
        );

        return updated;
    }

    public MedicalRecord assignVet(UUID recordId, User vetToAssign, User updatedBy) {
        MedicalRecord record = getById(recordId);
        if (record.getStatus().isFinal()) {
            throw new BusinessRuleException(
                    "Kan inte tilldela handläggare till ett stängt ärende");
        }

        if (vetToAssign.getRole() != Role.VET) {
            throw new BusinessRuleException(
                    "Endast veterinärer kan tilldelas som handläggare");
        }

        record.setAssignedVet(vetToAssign);
        record.setStatus(RecordStatus.IN_PROGRESS);
        record.setUpdatedBy(updatedBy);

//        return medicalRecordRepository.save(record);

        MedicalRecord updated = medicalRecordRepository.save(record);

        // LOGGING
        activityLogService.log(
                ActivityType.ASSIGNED,
                "Veterinär tilldelad",
                updatedBy,
                updated
        );

        return updated;


    }

    public MedicalRecord updateStatus(UUID recordId, RecordStatus newStatus, User updatedBy) {
        MedicalRecord record = getById(recordId);

        if (record.getStatus().isFinal()) {
            throw new BusinessRuleException("Stängda ärenden kan inte ändras");}
        record.setStatus(newStatus);
        record.setUpdatedBy(updatedBy);

        if (newStatus == RecordStatus.CLOSED) {
            record.setClosedAt(Instant.now());
        }

//        return medicalRecordRepository.save(record);

        MedicalRecord updated = medicalRecordRepository.save(record);

        // LOGGING
        activityLogService.log(
                ActivityType.STATUS_CHANGED,
                "Status ändrad till: " + newStatus,
                updatedBy,
                updated
        );

        return updated;

    }

    // ── Stänga ────────────────────────────────────────────────

    public MedicalRecord close(UUID recordId, User closedBy) {
        MedicalRecord record = getById(recordId);

        if (record.getStatus().isFinal()) {                                               // ← rad 108
            throw new BusinessRuleException("Ärendet är redan stängt");
        }

        record.setStatus(RecordStatus.CLOSED);
        record.setClosedAt(Instant.now());
        record.setUpdatedBy(closedBy);

//        return medicalRecordRepository.save(record);

        MedicalRecord updated = medicalRecordRepository.save(record);

        // LOGGING
        activityLogService.log(
                ActivityType.STATUS_CHANGED,
                "Ärende stängt",
                closedBy,
                updated
        );

        return updated;
    }

    public MedicalRecord save(MedicalRecord record) {
        return medicalRecordRepository.save(record);
    }
}