package org.example.vet1177.repository;

import org.example.vet1177.entities.MedicalRecord;
import org.example.vet1177.entities.RecordStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, UUID> {

    @EntityGraph(attributePaths = {
            "pet", "owner", "clinic", "assignedVet", "createdBy"
    })
    Optional<MedicalRecord> findById(UUID id);

    @EntityGraph(attributePaths = {"pet", "owner", "clinic", "assignedVet", "createdBy"})
    List<MedicalRecord> findByPetId(UUID petId);

    @EntityGraph(attributePaths = {"pet", "owner", "clinic", "assignedVet", "createdBy"})
    List<MedicalRecord> findByOwnerId(UUID ownerId);

    @EntityGraph(attributePaths = {"pet", "owner", "clinic", "assignedVet", "createdBy"})
    List<MedicalRecord> findByClinicId(UUID clinicId);

    @EntityGraph(attributePaths = {"pet", "owner", "clinic", "assignedVet", "createdBy"})
    List<MedicalRecord> findByClinicIdAndStatus(UUID clinicId, RecordStatus status);

    List<MedicalRecord> findByAssignedVetId(UUID vetId);
    List<MedicalRecord> findByStatus(RecordStatus status);
}