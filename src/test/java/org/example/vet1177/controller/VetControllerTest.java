package org.example.vet1177.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.vet1177.dto.request.vet.VetRequest;
import org.example.vet1177.dto.response.vet.VetResponse;
import org.example.vet1177.entities.User;
import org.example.vet1177.exception.ForbiddenException;
import org.example.vet1177.exception.ResourceNotFoundException;
import org.example.vet1177.policy.AdminPolicy;
import org.example.vet1177.services.VetService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VetController.class)
@AutoConfigureMockMvc(addFilters = false) // stänger av security
class VetControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private VetService vetService;

    @MockitoBean
    private AdminPolicy adminPolicy;

// =========================
// POST /api/vets
// =========================

    @Test
    void shouldCreateVet_whenAdmin() throws Exception {
        UUID userId = UUID.randomUUID();

        VetRequest request = new VetRequest(
                userId,
                "LIC123",
                "Surgery",
                "Available"
        );

        VetResponse response = new VetResponse(
                userId,
                "Dr. Smith",
                "test@test.com",
                "LIC123",
                "Surgery",
                "Available",
                "Clinic",
                true
        );

        doNothing().when(adminPolicy).requireAdmin(any(User.class));
        when(vetService.createVet(any())).thenReturn(response);

        mockMvc.perform(post("/api/vets")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Dr. Smith"))
                .andExpect(jsonPath("$.licenseId").value("LIC123"));
    }

    @Test
    void shouldReturn403_whenNotAdmin() throws Exception {
        VetRequest request = new VetRequest(
                UUID.randomUUID(),
                "LIC123",
                "Surgery",
                "Info"
        );

        doThrow(new ForbiddenException("Forbidden"))
                .when(adminPolicy)
                .requireAdmin(any());

        mockMvc.perform(post("/api/vets")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn400_whenInvalidRequest() throws Exception {
        VetRequest request = new VetRequest(
                null, // invalid
                "",
                "Surgery",
                "Info"
        );

        mockMvc.perform(post("/api/vets")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

// =========================
// GET /api/vets
// =========================

    @Test
    void shouldReturnAllVets() throws Exception {
        VetResponse response = new VetResponse(
                UUID.randomUUID(),
                "Dr. Smith",
                "test@test.com",
                "LIC123",
                "Surgery",
                "Available",
                "Clinic",
                true
        );

        when(vetService.getAllVets()).thenReturn(List.of(response));

        mockMvc.perform(get("/api/vets"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Dr. Smith"));
    }

// =========================
// GET /api/vets/{id}
// =========================

    @Test
    void shouldReturnVetById() throws Exception {
        UUID id = UUID.randomUUID();

        VetResponse response = new VetResponse(
                id,
                "Dr. Smith",
                "test@test.com",
                "LIC123",
                "Surgery",
                "Available",
                "Clinic",
                true
        );

        when(vetService.getVetById(id)).thenReturn(response);

        mockMvc.perform(get("/api/vets/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Dr. Smith"));
    }

    @Test
    void shouldReturn404_whenVetNotFound() throws Exception {
        UUID id = UUID.randomUUID();

        when(vetService.getVetById(id))
                .thenThrow(new ResourceNotFoundException("Vet", id));

        mockMvc.perform(get("/api/vets/" + id))
                .andExpect(status().isNotFound());
    }


}
