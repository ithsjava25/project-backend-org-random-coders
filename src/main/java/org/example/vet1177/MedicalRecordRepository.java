package org.example.vet1177;

import org.example.vet1177.entities.MedicalRecord;
import org.example.vet1177.entities.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MedicalRecordRepository extends JpaRepository<MedicalRecord, UUID> {

    // Hämta alla ärenden för ett specifikt djur
    List<MedicalRecord> findByPetId(UUID petId);

    // Hämta alla ärenden för en ägare
    List<MedicalRecord> findByOwnerId(UUID ownerId);

    // Hämta alla ärenden tilldelade en specifik vet
    List<MedicalRecord> findByAssignedVetId(UUID vetId);

    // Hämta alla ärenden för en klinik
    List<MedicalRecord> findByClinicId(UUID clinicId);

    // Hämta ärenden filtrerat på status
    List<MedicalRecord> findByStatus(RecordStatus status);

    // Kombinerat — ärenden för en klinik med specifik status (användbart för ABAC)
    List<MedicalRecord> findByClinicIdAndStatus(UUID clinicId, RecordStatus status);
}