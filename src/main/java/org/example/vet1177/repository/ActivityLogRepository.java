package org.example.vet1177.repository;

import org.example.vet1177.entities.ActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {

    List<ActivityLog> findByMedicalRecordIdOrderByCreatedAtDesc(UUID medicalRecordId);
}
