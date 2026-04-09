package org.example.vet1177.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import org.example.vet1177.dto.request.attachment.AttachmentRequest;
import org.example.vet1177.dto.response.attachment.AttachmentResponse;
import org.example.vet1177.entities.User;
import org.example.vet1177.services.AttachmentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    private static final Logger log = LoggerFactory.getLogger(AttachmentController.class);

    private final AttachmentService attachmentService;
    private final Validator validator;

    public AttachmentController(AttachmentService attachmentService, Validator validator) {

        this.attachmentService = attachmentService;
        this.validator = validator;

    }


    @PostMapping(value = "/record/{recordId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentResponse> uploadAttachment(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID recordId,
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "description", required = false) String description)  {


        log.info("POST /api/attachments/record/{} - uploading attachment", recordId);
        AttachmentRequest request = new AttachmentRequest(recordId, description);

        var violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }

        AttachmentResponse response = attachmentService.uploadAttachment(currentUser, file, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }


    @GetMapping("/record/{recordId}")
    public ResponseEntity<List<AttachmentResponse>> listAttachments(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID recordId) {

        log.info("GET /api/attachments/record/{}", recordId);
        List<AttachmentResponse> responses = attachmentService.getAttachmentsByRecord(currentUser, recordId);
        return ResponseEntity.ok(responses);
    }


    @GetMapping("/{id}/download")
    public ResponseEntity<AttachmentResponse> downloadAttachment(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {

        log.info("GET /api/attachments/{}/download", id);
        AttachmentResponse response = attachmentService.getAttachment(currentUser, id);
        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttachment(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {

        log.info("DELETE /api/attachments/{}", id);
        attachmentService.deleteAttachment(currentUser, id);
        return ResponseEntity.noContent().build();
    }
}