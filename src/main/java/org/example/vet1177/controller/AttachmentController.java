package org.example.vet1177.controller;


import org.example.vet1177.dto.request.attachment.AttachmentRequest;
import org.example.vet1177.dto.response.attachment.AttachmentResponse;
import org.example.vet1177.entities.User;
import org.example.vet1177.services.AttachmentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/attachments")
public class AttachmentController {

    private final AttachmentService attachmentService;

    public AttachmentController(AttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    /**
     * Krav: POST /api/attachments/record/{recordId}
     * Laddar upp en fil kopplad till en specifik journal.
     */
    @PostMapping(value = "/record/{recordId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentResponse> uploadAttachment(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID recordId,
            @RequestPart("file") MultipartFile file,
            @RequestPart("description") String description) throws IOException {

        // Skapar DTO:n internt för att skicka vidare till Service
        AttachmentRequest request = new AttachmentRequest(recordId, description);

        AttachmentResponse response = attachmentService.uploadAttachment(currentUser, file, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    /**
     * Krav: GET /api/attachments/record/{recordId}
     * Listar alla bilagor för en specifik journal.
     */
    @GetMapping("/record/{recordId}")
    public ResponseEntity<List<AttachmentResponse>> listAttachments(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID recordId) {

        List<AttachmentResponse> responses = attachmentService.getAttachmentsByRecord(currentUser, recordId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Krav: GET /api/attachments/{id}/download
     * Hämtar metadata och en presigned S3-länk för nedladdning.
     */
    @GetMapping("/{id}/download")
    public ResponseEntity<AttachmentResponse> downloadAttachment(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {

        AttachmentResponse response = attachmentService.getAttachment(currentUser, id);
        return ResponseEntity.ok(response);
    }

    /**
     * Krav: DELETE /api/attachments/{id}
     * Tar bort metadata i DB och filen i S3.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAttachment(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID id) {

        attachmentService.deleteAttachment(currentUser, id);
        return ResponseEntity.noContent().build();
    }
}