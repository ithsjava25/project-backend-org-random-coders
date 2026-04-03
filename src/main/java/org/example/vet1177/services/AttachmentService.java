package org.example.vet1177.services;

import org.example.vet1177.dto.request.attachment.AttachmentRequest;
import org.example.vet1177.dto.response.attachment.AttachmentResponse;
import org.example.vet1177.entities.Attachment;
import org.example.vet1177.entities.MedicalRecord;
import org.example.vet1177.entities.User;
import org.example.vet1177.exception.ResourceNotFoundException;
import org.example.vet1177.policy.AttachmentPolicy;
import org.example.vet1177.repository.AttachmentRepository;
import org.example.vet1177.repository.MedicalRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
public class AttachmentService {

    private static final Logger log = LoggerFactory.getLogger(AttachmentService.class);

    private final AttachmentRepository attachmentRepository;
    private final FileStorageService fileStorageService;
    private final MedicalRecordRepository medicalRecordRepository;
    private final AttachmentPolicy attachmentPolicy;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    // Manuell konstruktor för Dependency Injection
    public AttachmentService(AttachmentRepository attachmentRepository,
                             FileStorageService fileStorageService,
                             MedicalRecordRepository medicalRecordRepository,
                             AttachmentPolicy attachmentPolicy) {
        this.attachmentRepository = attachmentRepository;
        this.fileStorageService = fileStorageService;
        this.medicalRecordRepository = medicalRecordRepository;
        this.attachmentPolicy = attachmentPolicy;
    }

    /**
     * Laddar upp en bilaga, sparar i S3 och skapar metadata i databasen.
     */
    @Transactional
    public AttachmentResponse uploadAttachment(User currentUser, MultipartFile file, AttachmentRequest request) throws IOException {
        // 1. Hämta journalposten
        MedicalRecord record = medicalRecordRepository.findById(request.recordId())
                .orElseThrow(() -> new ResourceNotFoundException("MedicalRecord", request.recordId()));

        // 2. Validera behörighet och filregler (MIME-typ, 10MB gräns) via Policy
        attachmentPolicy.canUpload(currentUser, record, file.getContentType(), file.getSize());

        // 3. Skapa en unik S3-nyckel
        String s3Key = String.format("records/%s/%s_%s",
                record.getId(),
                UUID.randomUUID(),
                file.getOriginalFilename());

        log.debug("Uploading file {} to S3 with key {}", file.getOriginalFilename(), s3Key);

        // 4. Ladda upp till S3/MinIO
        fileStorageService.upload(s3Key, file.getInputStream(), file.getSize(), file.getContentType());

        // 5. Skapa och spara Attachment-entiteten
        Attachment attachment = new Attachment();
        attachment.setMedicalRecord(record);
        attachment.setUploadedBy(currentUser);
        attachment.setFileName(file.getOriginalFilename());
        attachment.setS3Key(s3Key);
        attachment.setS3Bucket(bucketName);
        attachment.setFileType(file.getContentType());
        attachment.setFileSizeBytes(file.getSize());
        attachment.setDescription(request.description());

        attachment = attachmentRepository.save(attachment);

        log.info("Attachment {} saved for record {}", attachment.getId(), record.getId());

        return mapToResponse(attachment);
    }

    /**
     * Hämtar metadata och en tidsbegränsad länk.
     */
    @Transactional(readOnly = true)
    public AttachmentResponse getAttachment(User currentUser, UUID attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", attachmentId));

        attachmentPolicy.canDownload(currentUser, attachment);

        return mapToResponse(attachment);
    }

    /**
     * Raderar både i S3 och i databasen.
     */
    @Transactional
    public void deleteAttachment(User currentUser, UUID attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", attachmentId));

        attachmentPolicy.canDelete(currentUser, attachment);

        // Radera fysiskt objekt
        fileStorageService.delete(attachment.getS3Key());

        // Radera i DB
        attachmentRepository.delete(attachment);

        log.info("Attachment {} deleted by user {}", attachmentId, currentUser.getId());
    }

    private AttachmentResponse mapToResponse(Attachment attachment) {
        String downloadUrl = fileStorageService.generatePresignedUrl(attachment.getS3Key());

        return new AttachmentResponse(
                attachment.getId(),
                attachment.getMedicalRecord().getId(),
                attachment.getFileName(),
                attachment.getDescription(),
                attachment.getFileType(),
                attachment.getFileSizeBytes(),
                attachment.getUploadedAt(),
                attachment.getUploadedBy() != null ? attachment.getUploadedBy().getName() : "Okänd",
                downloadUrl
        );
    }
}