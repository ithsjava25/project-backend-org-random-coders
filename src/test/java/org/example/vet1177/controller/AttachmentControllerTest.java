package org.example.vet1177.controller;

import org.example.vet1177.dto.response.attachment.AttachmentResponse;
import org.example.vet1177.entities.Role;
import org.example.vet1177.entities.User;
import org.example.vet1177.exception.ForbiddenException;
import org.example.vet1177.exception.ResourceNotFoundException;
import org.example.vet1177.security.CustomUserDetailsService;
import org.example.vet1177.security.JwtService;
import org.example.vet1177.security.SecurityConfig;
import org.example.vet1177.services.AttachmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AttachmentController.class)
@Import(SecurityConfig.class)
class AttachmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AttachmentService attachmentService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private User vetUser;
    private UUID recordId;
    private UUID attachmentId;
    private AttachmentResponse attachmentResponse;

    @BeforeEach
    void setUp() {
        recordId = UUID.randomUUID();
        attachmentId = UUID.randomUUID();

        vetUser = new User("Dr. Sara Lindqvist", "sara@vet.se", "hash", Role.VET);

        attachmentResponse = new AttachmentResponse(
                attachmentId,
                recordId,
                "bild.jpg",
                "En bild",
                "image/jpeg",
                1024L,
                Instant.now(),
                "Dr. Sara Lindqvist",
                "https://presigned-url"
        );
    }

    private RequestPostProcessor authenticatedAs(User user) {
        return authentication(new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities()
        ));
    }

    // -------------------------------------------------------------------------
    // POST /api/attachments/record/{recordId} — upload
    // -------------------------------------------------------------------------

    @Test
    void upload_shouldReturn201WithResponse() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "bild.jpg", "image/jpeg", new byte[]{1, 2, 3});

        when(attachmentService.uploadAttachment(any(User.class), any(), any()))
                .thenReturn(attachmentResponse);

        mockMvc.perform(multipart("/api/attachments/record/{recordId}", recordId)
                        .file(file)
                        .param("description", "En bild")
                        .with(authenticatedAs(vetUser)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").value("bild.jpg"))
                .andExpect(jsonPath("$.downloadUrl").value("https://presigned-url"));
    }

    @Test
    void upload_whenRecordNotFound_shouldReturn404() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "bild.jpg", "image/jpeg", new byte[]{1, 2, 3});

        when(attachmentService.uploadAttachment(any(), any(), any()))
                .thenThrow(new ResourceNotFoundException("MedicalRecord", recordId));

        mockMvc.perform(multipart("/api/attachments/record/{recordId}", recordId)
                        .file(file)
                        .with(authenticatedAs(vetUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    void upload_whenForbidden_shouldReturn403() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "bild.jpg", "image/jpeg", new byte[]{1, 2, 3});

        when(attachmentService.uploadAttachment(any(), any(), any()))
                .thenThrow(new ForbiddenException("Du saknar behörighet"));

        mockMvc.perform(multipart("/api/attachments/record/{recordId}", recordId)
                        .file(file)
                        .with(authenticatedAs(vetUser)))
                .andExpect(status().isForbidden());
    }

    @Test
    void upload_withoutFile_shouldReturn400() throws Exception {
        mockMvc.perform(multipart("/api/attachments/record/{recordId}", recordId)
                        .with(authenticatedAs(vetUser)))
                .andExpect(status().isBadRequest());
    }

    // -------------------------------------------------------------------------
    // GET /api/attachments/record/{recordId} — listByRecord
    // -------------------------------------------------------------------------

    @Test
    void listByRecord_shouldReturn200WithList() throws Exception {
        when(attachmentService.getAttachmentsByRecord(any(User.class), eq(recordId)))
                .thenReturn(List.of(attachmentResponse));

        mockMvc.perform(get("/api/attachments/record/{recordId}", recordId)
                        .with(authenticatedAs(vetUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fileName").value("bild.jpg"));
    }

    @Test
    void listByRecord_whenRecordNotFound_shouldReturn404() throws Exception {
        when(attachmentService.getAttachmentsByRecord(any(), any()))
                .thenThrow(new ResourceNotFoundException("MedicalRecord", recordId));

        mockMvc.perform(get("/api/attachments/record/{recordId}", recordId)
                        .with(authenticatedAs(vetUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    void listByRecord_whenForbidden_shouldReturn403() throws Exception {
        when(attachmentService.getAttachmentsByRecord(any(), any()))
                .thenThrow(new ForbiddenException("Du saknar behörighet"));

        mockMvc.perform(get("/api/attachments/record/{recordId}", recordId)
                        .with(authenticatedAs(vetUser)))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    // GET /api/attachments/{id}/download — download
    // -------------------------------------------------------------------------

    @Test
    void download_shouldReturn200WithResponse() throws Exception {
        when(attachmentService.getAttachment(any(User.class), eq(attachmentId)))
                .thenReturn(attachmentResponse);

        mockMvc.perform(get("/api/attachments/{id}/download", attachmentId)
                        .with(authenticatedAs(vetUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fileName").value("bild.jpg"))
                .andExpect(jsonPath("$.downloadUrl").value("https://presigned-url"));
    }

    @Test
    void download_whenNotFound_shouldReturn404() throws Exception {
        when(attachmentService.getAttachment(any(), any()))
                .thenThrow(new ResourceNotFoundException("Attachment", attachmentId));

        mockMvc.perform(get("/api/attachments/{id}/download", attachmentId)
                        .with(authenticatedAs(vetUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    void download_whenForbidden_shouldReturn403() throws Exception {
        when(attachmentService.getAttachment(any(), any()))
                .thenThrow(new ForbiddenException("Du saknar behörighet"));

        mockMvc.perform(get("/api/attachments/{id}/download", attachmentId)
                        .with(authenticatedAs(vetUser)))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    // DELETE /api/attachments/{id} — delete
    // -------------------------------------------------------------------------

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(attachmentService).deleteAttachment(any(User.class), eq(attachmentId));

        mockMvc.perform(delete("/api/attachments/{id}", attachmentId)
                        .with(authenticatedAs(vetUser)))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenNotFound_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Attachment", attachmentId))
                .when(attachmentService).deleteAttachment(any(), any());

        mockMvc.perform(delete("/api/attachments/{id}", attachmentId)
                        .with(authenticatedAs(vetUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_whenForbidden_shouldReturn403() throws Exception {
        doThrow(new ForbiddenException("Du saknar behörighet"))
                .when(attachmentService).deleteAttachment(any(), any());

        mockMvc.perform(delete("/api/attachments/{id}", attachmentId)
                        .with(authenticatedAs(vetUser)))
                .andExpect(status().isForbidden());
    }
}
