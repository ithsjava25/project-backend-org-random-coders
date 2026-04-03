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


    @Transactional
    public AttachmentResponse uploadAttachment(User currentUser, MultipartFile file, AttachmentRequest request) throws IOException {
        MedicalRecord record = medicalRecordRepository.findById(request.recordId())
                .orElseThrow(() -> new ResourceNotFoundException("MedicalRecord", request.recordId()));

        // Validering
        attachmentPolicy.canUpload(currentUser, record, file.getContentType(), file.getSize());

        // Skapa unik S3-nyckel
        String s3Key = String.format("records/%s/%s_%s",
                record.getId(),
                UUID.randomUUID(),
                file.getOriginalFilename());

        // Anropa FileStorageService
        fileStorageService.upload(s3Key, file.getInputStream(), file.getSize(), file.getContentType());

        // Skapa entitet
        Attachment attachment = new Attachment();
        attachment.setMedicalRecord(record);
        attachment.setUploadedBy(currentUser);
        attachment.setFileName(file.getOriginalFilename());
        attachment.setS3Key(s3Key);
        attachment.setS3Bucket(bucketName); // Använder namnet från AwsS3Properties
        attachment.setFileType(file.getContentType());
        attachment.setFileSizeBytes(file.getSize());
        attachment.setDescription(request.description());

        attachment = attachmentRepository.save(attachment);

        log.info("Attachment {} successfully saved for record {}", attachment.getId(), record.getId());

        return mapToResponse(attachment);
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

        // Radera objekt i S3
        fileStorageService.delete(attachment.getS3Key());

        // Radera rad i DB
        attachmentRepository.delete(attachment);

        log.info("Attachment {} deleted from storage and database", attachmentId);
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