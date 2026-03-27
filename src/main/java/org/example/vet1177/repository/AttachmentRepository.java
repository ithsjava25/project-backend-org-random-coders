package org.example.vet1177.repository;

import org.example.vet1177.entities.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AttachmentRepository extends JpaRepository<Attachment, UUID> {

    List<Attachment> findByMedicalRecordId(UUID recordId);

    List<Attachment> findByUploadedById(UUID userId);

    Optional<Attachment> findByS3Key(String s3Key);
}
