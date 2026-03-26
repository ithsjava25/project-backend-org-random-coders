package org.example.vet1177.services;

import org.example.vet1177.entities.*;
import org.example.vet1177.repositories.MedicalRecordRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class MedicalRecordService {

    private final MedicalRecordRepository medicalRecordRepository;

    public MedicalRecordService(MedicalRecordRepository medicalRecordRepository) {
        this.medicalRecordRepository = medicalRecordRepository;
    }

    // ── Skapa ────────────────────────────────────────────────

    public MedicalRecord create(
            String title,
            String description,
            Pet pet,
            User owner,
            Clinic clinic,
            User createdBy) {

        MedicalRecord record = new MedicalRecord();
        record.setTitle(title);
        record.setDescription(description);
        record.setPet(pet);
        record.setOwner(owner);
        record.setClinic(clinic);
        record.setCreatedBy(createdBy);
        record.setStatus(RecordStatus.OPEN);

        return medicalRecordRepository.save(record);
    }

    // ── Läsa ─────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public MedicalRecord getById(UUID id) {
        return medicalRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ärende hittades inte: " + id));
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

    // ── Uppdatera ─────────────────────────────────────────────

    public MedicalRecord update(UUID id, String title, String description, User updatedBy) {
        MedicalRecord record = getById(id);
        record.setTitle(title);
        record.setDescription(description);
        record.setUpdatedBy(updatedBy);
        return medicalRecordRepository.save(record);
    }

    public MedicalRecord assignVet(UUID recordId, User vet, User updatedBy) {
        MedicalRecord record = getById(recordId);
        record.setAssignedVet(vet);
        record.setStatus(RecordStatus.IN_PROGRESS);
        record.setUpdatedBy(updatedBy);
        return medicalRecordRepository.save(record);
    }

    public MedicalRecord updateStatus(UUID recordId, RecordStatus newStatus, User updatedBy) {
        MedicalRecord record = getById(recordId);
        record.setStatus(newStatus);
        record.setUpdatedBy(updatedBy);

        if (newStatus == RecordStatus.CLOSED) {
            record.setClosedAt(Instant.now());
        }

        return medicalRecordRepository.save(record);
    }

    // ── Stänga ────────────────────────────────────────────────

    public MedicalRecord close(UUID recordId, User closedBy) {
        MedicalRecord record = getById(recordId);

        if (record.getStatus() == RecordStatus.CLOSED) {
            throw new RuntimeException("Ärendet är redan stängt");
        }

        record.setStatus(RecordStatus.CLOSED);
        record.setClosedAt(Instant.now());
        record.setUpdatedBy(closedBy);

        return medicalRecordRepository.save(record);
    }
}