package org.example.vet1177.services;

import org.example.vet1177.config.AwsS3Properties;
import org.example.vet1177.dto.request.attachment.AttachmentRequest;
import org.example.vet1177.dto.response.attachment.AttachmentResponse;
import org.example.vet1177.entities.*;
import org.example.vet1177.exception.ResourceNotFoundException;
import org.example.vet1177.policy.AttachmentPolicy;
import org.example.vet1177.policy.MedicalRecordPolicy;
import org.example.vet1177.repository.AttachmentRepository;
import org.example.vet1177.repository.MedicalRecordRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AttachmentServiceTest {

    @Mock
    private AttachmentRepository attachmentRepository;

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @Mock
    private AttachmentPolicy attachmentPolicy;

    @Mock
    private MedicalRecordPolicy medicalRecordPolicy;

    private AttachmentService attachmentService;

    private User vetUser;
    private MedicalRecord record;
    private Attachment attachment;
    private UUID recordId;
    private UUID attachmentId;

    @BeforeEach
    void setUp() {
        recordId = UUID.randomUUID();
        attachmentId = UUID.randomUUID();

        AwsS3Properties props = new AwsS3Properties();
        props.setBucketName("test-bucket");

        attachmentService = new AttachmentService(
                attachmentRepository,
                fileStorageService,
                medicalRecordRepository,
                attachmentPolicy,
                props,
                medicalRecordPolicy
        );

        vetUser = new User("Dr. Sara Lindqvist", "sara@vet.se", "hash", Role.VET);

        record = new MedicalRecord();
        record.setId(recordId);
        record.setTitle("Halsont");
        record.setStatus(RecordStatus.OPEN);

        attachment = new Attachment();
        ReflectionTestUtils.setField(attachment, "id", attachmentId);
        attachment.setMedicalRecord(record);
        attachment.setUploadedBy(vetUser);
        attachment.setFileName("bild.jpg");
        attachment.setS3Key("records/" + recordId + "/abc_bild.jpg");
        attachment.setS3Bucket("test-bucket");
        attachment.setFileType("image/jpeg");
        attachment.setFileSizeBytes(1024L);
        attachment.setDescription("En bild");
    }

    // -------------------------------------------------------------------------
    // uploadAttachment
    // -------------------------------------------------------------------------

    @Test
    void upload_shouldPersistAndReturnResponse() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "bild.jpg", "image/jpeg", new byte[]{1, 2, 3});

        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(attachmentRepository.saveAndFlush(any(Attachment.class))).thenReturn(attachment);
        when(fileStorageService.generatePresignedUrl(anyString())).thenReturn("https://presigned-url");

        AttachmentResponse response = attachmentService.uploadAttachment(
                vetUser, file, new AttachmentRequest(recordId, "En bild"));

        assertThat(response.fileName()).isEqualTo("bild.jpg");
        assertThat(response.downloadUrl()).isEqualTo("https://presigned-url");
        verify(attachmentRepository).saveAndFlush(any(Attachment.class));
    }

    @Test
    void upload_shouldCallFileStorageUpload() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "bild.jpg", "image/jpeg", new byte[]{1, 2, 3});

        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(attachmentRepository.saveAndFlush(any(Attachment.class))).thenReturn(attachment);
        when(fileStorageService.generatePresignedUrl(anyString())).thenReturn("https://url");

        attachmentService.uploadAttachment(vetUser, file, new AttachmentRequest(recordId, "En bild"));

        verify(fileStorageService).upload(anyString(), any(), eq(3L), eq("image/jpeg"));
    }

    @Test
    void upload_shouldCallPolicyCanUpload() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "bild.jpg", "image/jpeg", new byte[]{1, 2, 3});

        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(attachmentRepository.saveAndFlush(any(Attachment.class))).thenReturn(attachment);
        when(fileStorageService.generatePresignedUrl(anyString())).thenReturn("https://url");

        attachmentService.uploadAttachment(vetUser, file, new AttachmentRequest(recordId, "En bild"));

        verify(attachmentPolicy).canUpload(vetUser, record, "image/jpeg", 3L);
    }

    @Test
    void upload_whenRecordNotFound_shouldThrowResourceNotFoundException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "bild.jpg", "image/jpeg", new byte[]{1, 2, 3});

        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attachmentService.uploadAttachment(
                vetUser, file, new AttachmentRequest(recordId, "En bild")))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(attachmentRepository, never()).saveAndFlush(any());
    }

    @Test
    void upload_whenFileIsEmpty_shouldThrowIllegalArgumentException() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "tom.jpg", "image/jpeg", new byte[0]);

        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(record));

        assertThatThrownBy(() -> attachmentService.uploadAttachment(
                vetUser, file, new AttachmentRequest(recordId, "Tom fil")))
                .isInstanceOf(IllegalArgumentException.class);

        verify(fileStorageService, never()).upload(any(), any(), anyLong(), any());
    }

    @Test
    void upload_whenS3Fails_shouldThrowAndNotPersist() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "bild.jpg", "image/jpeg", new byte[]{1, 2, 3});

        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(record));
        doThrow(new RuntimeException("S3 down"))
                .when(fileStorageService).upload(anyString(), any(), anyLong(), anyString());

        assertThatThrownBy(() -> attachmentService.uploadAttachment(
                vetUser, file, new AttachmentRequest(recordId, "En bild")))
                .isInstanceOf(RuntimeException.class);

        verify(attachmentRepository, never()).saveAndFlush(any());
    }

    // -------------------------------------------------------------------------
    // getAttachment
    // -------------------------------------------------------------------------

    @Test
    void getAttachment_shouldReturnResponse() {
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));
        when(fileStorageService.generatePresignedUrl(attachment.getS3Key()))
                .thenReturn("https://presigned-url");

        AttachmentResponse response = attachmentService.getAttachment(vetUser, attachmentId);

        assertThat(response.id()).isEqualTo(attachmentId);
        assertThat(response.fileName()).isEqualTo("bild.jpg");
        assertThat(response.downloadUrl()).isEqualTo("https://presigned-url");
    }

    @Test
    void getAttachment_shouldCallPolicyCanDownload() {
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));
        when(fileStorageService.generatePresignedUrl(anyString())).thenReturn("https://url");

        attachmentService.getAttachment(vetUser, attachmentId);

        verify(attachmentPolicy).canDownload(vetUser, attachment);
    }

    @Test
    void getAttachment_whenNotFound_shouldThrowResourceNotFoundException() {
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attachmentService.getAttachment(vetUser, attachmentId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // getAttachmentsByRecord
    // -------------------------------------------------------------------------

    @Test
    void getByRecord_shouldReturnListOfResponses() {
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(attachmentRepository.findByMedicalRecordId(recordId)).thenReturn(List.of(attachment));
        when(fileStorageService.generatePresignedUrl(anyString())).thenReturn("https://url");

        List<AttachmentResponse> responses = attachmentService.getAttachmentsByRecord(vetUser, recordId);

        assertThat(responses).hasSize(1);
        assertThat(responses.getFirst().fileName()).isEqualTo("bild.jpg");
    }

    @Test
    void getByRecord_shouldCallMedicalRecordPolicyCanView() {
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(attachmentRepository.findByMedicalRecordId(recordId)).thenReturn(List.of());

        attachmentService.getAttachmentsByRecord(vetUser, recordId);

        verify(medicalRecordPolicy).canView(vetUser, record);
    }

    @Test
    void getByRecord_whenRecordNotFound_shouldThrowResourceNotFoundException() {
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attachmentService.getAttachmentsByRecord(vetUser, recordId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(attachmentRepository, never()).findByMedicalRecordId(any());
    }

    // -------------------------------------------------------------------------
    // deleteAttachment
    // -------------------------------------------------------------------------

    @Test
    void delete_shouldDeleteFromRepository() {
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));

        attachmentService.deleteAttachment(vetUser, attachmentId);

        verify(attachmentRepository).delete(attachment);
    }

    @Test
    void delete_shouldCallPolicyCanDelete() {
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));

        attachmentService.deleteAttachment(vetUser, attachmentId);

        verify(attachmentPolicy).canDelete(vetUser, attachment);
    }

    @Test
    void delete_shouldCallFileStorageDelete() {
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));

        attachmentService.deleteAttachment(vetUser, attachmentId);

        verify(fileStorageService).delete(attachment.getS3Key());
    }

    @Test
    void delete_whenNotFound_shouldThrowResourceNotFoundException() {
        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> attachmentService.deleteAttachment(vetUser, attachmentId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(attachmentRepository, never()).delete(any());
    }
}
