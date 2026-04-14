package org.example.vet1177.controller;

import tools.jackson.databind.ObjectMapper;
import org.example.vet1177.dto.request.comment.CreateCommentRequest;
import org.example.vet1177.dto.request.comment.UpdateCommentRequest;
import org.example.vet1177.entities.Comment;
import org.example.vet1177.entities.MedicalRecord;
import org.example.vet1177.entities.Role;
import org.example.vet1177.entities.User;
import org.example.vet1177.exception.ForbiddenException;
import org.example.vet1177.exception.ResourceNotFoundException;
import org.example.vet1177.services.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.example.vet1177.security.CustomUserDetailsService;
import org.example.vet1177.security.JwtService;
import org.example.vet1177.security.SecurityConfig;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
@Import(SecurityConfig.class)
class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private User currentUser;
    private MedicalRecord record;
    private Comment comment;
    private UUID recordId;
    private UUID commentId;

    @BeforeEach
    void setUp() {
        recordId = UUID.randomUUID();
        commentId = UUID.randomUUID();

        currentUser = new User("Dr. Sara Lindqvist", "sara@vet.se", "hash", Role.VET);

        record = new MedicalRecord();
        record.setId(recordId);

        comment = new Comment();
        comment.setBody("En kommentar.");
        comment.setAuthor(currentUser);
        comment.setMedicalRecord(record);
    }

    private RequestPostProcessor authenticatedAs(User user) {
        return authentication(new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities()
        ));
    }

    // -------------------------------------------------------------------------
    // POST /api/comments — create
    // -------------------------------------------------------------------------

    @Test
    void create_shouldReturn200WithCommentResponse() throws Exception {
        when(commentService.create(eq(recordId), eq("En kommentar."), any(User.class)))
                .thenReturn(comment);

        var request = new CreateCommentRequest(recordId, "En kommentar.");

        mockMvc.perform(post("/api/comments")
                        .with(authenticatedAs(currentUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body").value("En kommentar."))
                .andExpect(jsonPath("$.authorName").value("Dr. Sara Lindqvist"));
    }

    @Test
    void create_whenBodyIsBlank_shouldReturn400() throws Exception {
        var request = new CreateCommentRequest(recordId, "   ");

        mockMvc.perform(post("/api/comments")
                        .with(authenticatedAs(currentUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_whenRecordIdIsNull_shouldReturn400() throws Exception {
        var request = new CreateCommentRequest(null, "En kommentar.");

        mockMvc.perform(post("/api/comments")
                        .with(authenticatedAs(currentUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_whenRecordNotFound_shouldReturn404() throws Exception {
        when(commentService.create(any(), any(), any()))
                .thenThrow(new ResourceNotFoundException("MedicalRecord", recordId));

        var request = new CreateCommentRequest(recordId, "En kommentar.");

        mockMvc.perform(post("/api/comments")
                        .with(authenticatedAs(currentUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_whenForbidden_shouldReturn403() throws Exception {
        when(commentService.create(any(), any(), any()))
                .thenThrow(new ForbiddenException("Du saknar behörighet"));

        var request = new CreateCommentRequest(recordId, "En kommentar.");

        mockMvc.perform(post("/api/comments")
                        .with(authenticatedAs(currentUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    // GET /api/comments/record/{recordId} — getByRecord
    // -------------------------------------------------------------------------

    @Test
    void getByRecord_shouldReturn200WithListOfComments() throws Exception {
        when(commentService.getByRecord(eq(recordId), any(User.class)))
                .thenReturn(List.of(comment));

        mockMvc.perform(get("/api/comments/record/{recordId}", recordId)
                        .with(authenticatedAs(currentUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].body").value("En kommentar."));
    }

    @Test
    void getByRecord_whenNoComments_shouldReturnEmptyList() throws Exception {
        when(commentService.getByRecord(eq(recordId), any(User.class)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/comments/record/{recordId}", recordId)
                        .with(authenticatedAs(currentUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    void getByRecord_whenRecordNotFound_shouldReturn404() throws Exception {
        when(commentService.getByRecord(any(), any()))
                .thenThrow(new ResourceNotFoundException("MedicalRecord", recordId));

        mockMvc.perform(get("/api/comments/record/{recordId}", recordId)
                        .with(authenticatedAs(currentUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getByRecord_whenForbidden_shouldReturn403() throws Exception {
        when(commentService.getByRecord(any(), any()))
                .thenThrow(new ForbiddenException("Du saknar behörighet"));

        mockMvc.perform(get("/api/comments/record/{recordId}", recordId)
                        .with(authenticatedAs(currentUser)))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    // PUT /api/comments/{id} — update
    // -------------------------------------------------------------------------

    @Test
    void update_shouldReturn200WithUpdatedComment() throws Exception {
        comment.setBody("Uppdaterad text.");
        when(commentService.update(eq(commentId), eq("Uppdaterad text."), any(User.class)))
                .thenReturn(comment);

        var request = new UpdateCommentRequest("Uppdaterad text.");

        mockMvc.perform(put("/api/comments/{id}", commentId)
                        .with(authenticatedAs(currentUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.body").value("Uppdaterad text."));
    }

    @Test
    void update_whenBodyIsBlank_shouldReturn400() throws Exception {
        var request = new UpdateCommentRequest("   ");

        mockMvc.perform(put("/api/comments/{id}", commentId)
                        .with(authenticatedAs(currentUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_whenCommentNotFound_shouldReturn404() throws Exception {
        when(commentService.update(any(), any(), any()))
                .thenThrow(new ResourceNotFoundException("Comment", commentId));

        var request = new UpdateCommentRequest("Uppdaterad text.");

        mockMvc.perform(put("/api/comments/{id}", commentId)
                        .with(authenticatedAs(currentUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_whenForbidden_shouldReturn403() throws Exception {
        when(commentService.update(any(), any(), any()))
                .thenThrow(new ForbiddenException("Du saknar behörighet"));

        var request = new UpdateCommentRequest("Uppdaterad text.");

        mockMvc.perform(put("/api/comments/{id}", commentId)
                        .with(authenticatedAs(currentUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    // DELETE /api/comments/{id} — delete
    // -------------------------------------------------------------------------

    @Test
    void delete_shouldReturn204() throws Exception {
        doNothing().when(commentService).delete(eq(commentId), any(User.class));

        mockMvc.perform(delete("/api/comments/{id}", commentId)
                        .with(authenticatedAs(currentUser)))
                .andExpect(status().isNoContent());
    }

    @Test
    void delete_whenCommentNotFound_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Comment", commentId))
                .when(commentService).delete(any(), any());

        mockMvc.perform(delete("/api/comments/{id}", commentId)
                        .with(authenticatedAs(currentUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    void delete_whenForbidden_shouldReturn403() throws Exception {
        doThrow(new ForbiddenException("Du saknar behörighet"))
                .when(commentService).delete(any(), any());

        mockMvc.perform(delete("/api/comments/{id}", commentId)
                        .with(authenticatedAs(currentUser)))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    // GET /api/comments/record/{recordId}/count — countByRecord
    // -------------------------------------------------------------------------

    @Test
    void countByRecord_shouldReturn200WithCount() throws Exception {
        when(commentService.countByRecord(eq(recordId), any(User.class)))
                .thenReturn(3L);

        mockMvc.perform(get("/api/comments/record/{recordId}/count", recordId)
                        .with(authenticatedAs(currentUser)))
                .andExpect(status().isOk())
                .andExpect(content().string("3"));
    }

    @Test
    void countByRecord_whenRecordNotFound_shouldReturn404() throws Exception {
        when(commentService.countByRecord(any(), any()))
                .thenThrow(new ResourceNotFoundException("MedicalRecord", recordId));

        mockMvc.perform(get("/api/comments/record/{recordId}/count", recordId)
                        .with(authenticatedAs(currentUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    void countByRecord_whenForbidden_shouldReturn403() throws Exception {
        when(commentService.countByRecord(any(), any()))
                .thenThrow(new ForbiddenException("Du saknar behörighet"));

        mockMvc.perform(get("/api/comments/record/{recordId}/count", recordId)
                        .with(authenticatedAs(currentUser)))
                .andExpect(status().isForbidden());
    }
}
