package org.example.vet1177.services;

import org.example.vet1177.config.AwsS3Properties;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
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
    private final String bucketName;

    public AttachmentService(AttachmentRepository attachmentRepository,
                             FileStorageService fileStorageService,
                             MedicalRecordRepository medicalRecordRepository,
                             AttachmentPolicy attachmentPolicy,
                             AwsS3Properties props) {
        this.attachmentRepository = attachmentRepository;
        this.fileStorageService = fileStorageService;
        this.medicalRecordRepository = medicalRecordRepository;
        this.attachmentPolicy = attachmentPolicy;
        this.bucketName = props.getBucketName();
    }


    @Transactional(rollbackFor = Exception.class)
    public AttachmentResponse uploadAttachment(User currentUser, MultipartFile file, AttachmentRequest request) throws IOException {
        MedicalRecord record = medicalRecordRepository.findById(request.recordId())
                .orElseThrow(() -> new ResourceNotFoundException("MedicalRecord", request.recordId()));

        if (file.isEmpty() || file.getSize() <= 0) {
            throw new IllegalArgumentException("Det går inte att ladda upp en tom fil.");
        }

        // Validering
        attachmentPolicy.canUpload(currentUser, record, file.getContentType(), file.getSize());

        String originalName = file.getOriginalFilename();
        String sanitizedName = sanitizeFilename(originalName);

        // Skapa unik S3-nyckel
        String s3Key = String.format("records/%s/%s_%s",
                record.getId(),
                UUID.randomUUID(),
                file.getOriginalFilename());

        // Anropa FileStorageService
        try {
            fileStorageService.upload(s3Key, file.getInputStream(), file.getSize(), file.getContentType());
        } catch (Exception e) {
            log.error("S3 upload failed for key: {}", s3Key);
            throw new RuntimeException("Kunde inte ladda upp filen till lagringen", e);
        }

        // Skapa entitet
    try {
        Attachment attachment = new Attachment();
        attachment.setMedicalRecord(record);
        attachment.setUploadedBy(currentUser);
        attachment.setFileName(file.getOriginalFilename());
        attachment.setS3Key(s3Key);
        attachment.setS3Bucket(bucketName);
        attachment.setFileType(file.getContentType());
        attachment.setFileSizeBytes(file.getSize());
        attachment.setDescription(request.description());

        attachment = attachmentRepository.saveAndFlush(attachment);

        log.info("Attachment {} successfully persisted for record {}", attachment.getId(), record.getId());

        return mapToResponse(attachment);

        } catch (Exception e) {
        // Tack vare saveAndFlush hamnar vi här om databasen nekar sparningen
        log.error("Database persistence failed for attachment with S3 key: {}. Triggering S3 cleanup.", s3Key);

        try {
            fileStorageService.delete(s3Key);
        } catch (Exception deleteEx) {
            log.error("CRITICAL: Failed to cleanup S3 object {} after DB failure!", s3Key, deleteEx);
        }

        throw new RuntimeException("Kunde inte spara bilagans metadata. Uppladdningen avbröts.", e);
        }
    }

    private String sanitizeFilename(String originalFilename) {
        if (originalFilename == null || originalFilename.isBlank()) {
            return UUID.randomUUID().toString();
        }

        String filename = new java.io.File(originalFilename).getName();
        filename = filename.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");

        filename = filename.trim();
            if (filename.isEmpty() || filename.equals(".") || filename.equals("..")) {
                return "file_" + System.currentTimeMillis();
            }
            return filename;
    }


    @Transactional(readOnly = true)
    public AttachmentResponse getAttachment(User currentUser, UUID attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", attachmentId));

        attachmentPolicy.canDownload(currentUser, attachment);

        return mapToResponse(attachment);
    }


    @Transactional
    public void deleteAttachment(User currentUser, UUID attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Attachment", attachmentId));

        attachmentPolicy.canDelete(currentUser, attachment);

        String s3Key = attachment.getS3Key();

        attachmentRepository.delete(attachment);

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        fileStorageService.delete(s3Key);
                        log.info("S3 object {} deleted after successful DB commit", s3Key);
                    } catch (Exception e) {
                        log.error("CRITICAL: Failed to delete S3 object {} after DB commit!", s3Key, e);
                    }
                }
            });
        } else {
            fileStorageService.delete(s3Key);
        }

        log.info("Attachment {} marked for deletion in database", attachmentId);
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
                attachment.getUploadedBy() != null ? attachment.getUploadedBy().getName() : "System",
                downloadUrl
        );
    }
}