package org.example.vet1177.controller;

import tools.jackson.databind.ObjectMapper;
import org.example.vet1177.dto.request.medicalrecord.AssignVetRequest;
import org.example.vet1177.dto.request.medicalrecord.CreateMedicalRecordRequest;
import org.example.vet1177.dto.request.medicalrecord.UpdateMedicalRecordRequest;
import org.example.vet1177.dto.request.medicalrecord.UpdateStatusRequest;
import org.example.vet1177.entities.Clinic;
import org.example.vet1177.entities.MedicalRecord;
import org.example.vet1177.entities.Pet;
import org.example.vet1177.entities.RecordStatus;
import org.example.vet1177.entities.Role;
import org.example.vet1177.entities.User;
import org.example.vet1177.exception.BusinessRuleException;
import org.example.vet1177.exception.ForbiddenException;
import org.example.vet1177.exception.ResourceNotFoundException;
import org.example.vet1177.policy.MedicalRecordPolicy;
import org.example.vet1177.security.CustomUserDetailsService;
import org.example.vet1177.security.JwtService;
import org.example.vet1177.security.SecurityConfig;
import org.example.vet1177.services.MedicalRecordService;
import org.example.vet1177.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MedicalRecordController.class)
@Import(SecurityConfig.class)
class MedicalRecordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MedicalRecordService medicalRecordService;

    @MockitoBean
    private MedicalRecordPolicy medicalRecordPolicy;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private User vetUser;
    private User ownerUser;
    private Pet pet;
    private Clinic clinic;
    private MedicalRecord record;
    private UUID recordId;
    private UUID petId;
    private UUID clinicId;

    @BeforeEach
    void setUp() {
        recordId = UUID.randomUUID();
        petId = UUID.randomUUID();
        clinicId = UUID.randomUUID();

        vetUser = new User("Dr. Sara Lindqvist", "sara@vet.se", "hash", Role.VET);
        ownerUser = new User("Anna Ägare", "anna@example.se", "hash", Role.OWNER);
        ReflectionTestUtils.setField(ownerUser, "id", UUID.randomUUID());

        pet = new Pet();
        pet.setName("Bella");
        pet.setSpecies("Hund");
        pet.setOwner(ownerUser);

        clinic = new Clinic();
        clinic.setName("Stadens Veterinär");

        record = new MedicalRecord();
        record.setId(recordId);
        record.setTitle("Halsont");
        record.setDescription("Hostar mycket");
        record.setStatus(RecordStatus.OPEN);
        record.setPet(pet);
        record.setOwner(ownerUser);
        record.setClinic(clinic);
        record.setCreatedBy(vetUser);
    }

    private RequestPostProcessor authenticatedAs(User user) {
        return authentication(new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities()
        ));
    }

    // -------------------------------------------------------------------------
    // POST /api/medical-records — create
    // -------------------------------------------------------------------------

    @Test
    void create_shouldReturn200WithMedicalRecordResponse() throws Exception {
        when(medicalRecordService.create(eq("Halsont"), eq("Hostar mycket"), eq(petId), eq(clinicId), any(User.class)))
                .thenReturn(record);

        var request = new CreateMedicalRecordRequest("Halsont", "Hostar mycket", petId, clinicId);

        mockMvc.perform(post("/api/medical-records")
                        .with(authenticatedAs(vetUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Halsont"))
                .andExpect(jsonPath("$.status").value("OPEN"))
                .andExpect(jsonPath("$.petName").value("Bella"));
    }

    @Test
    void create_whenTitleIsBlank_shouldReturn400() throws Exception {
        var request = new CreateMedicalRecordRequest("   ", "x", petId, clinicId);

        mockMvc.perform(post("/api/medical-records")
                        .with(authenticatedAs(vetUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_whenPetIdIsNull_shouldReturn400() throws Exception {
        var request = new CreateMedicalRecordRequest("Halsont", "x", null, clinicId);

        mockMvc.perform(post("/api/medical-records")
                        .with(authenticatedAs(vetUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_whenClinicIdIsNull_shouldReturn400() throws Exception {
        var request = new CreateMedicalRecordRequest("Halsont", "x", petId, null);

        mockMvc.perform(post("/api/medical-records")
                        .with(authenticatedAs(vetUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void create_whenPetNotFound_shouldReturn404() throws Exception {
        when(medicalRecordService.create(any(), any(), any(), any(), any()))
                .thenThrow(new ResourceNotFoundException("Pet", petId));

        var request = new CreateMedicalRecordRequest("Halsont", "x", petId, clinicId);

        mockMvc.perform(post("/api/medical-records")
                        .with(authenticatedAs(vetUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void create_whenForbidden_shouldReturn403() throws Exception {
        when(medicalRecordService.create(any(), any(), any(), any(), any()))
                .thenThrow(new ForbiddenException("Du saknar behörighet"));

        var request = new CreateMedicalRecordRequest("Halsont", "x", petId, clinicId);

        mockMvc.perform(post("/api/medical-records")
                        .with(authenticatedAs(vetUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    // GET /api/medical-records/{id} — getById
    // -------------------------------------------------------------------------

    @Test
    void getById_shouldReturn200WithMedicalRecordResponse() throws Exception {
        when(medicalRecordService.getById(recordId)).thenReturn(record);

        mockMvc.perform(get("/api/medical-records/{id}", recordId)
                        .with(authenticatedAs(vetUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Halsont"))
                .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void getById_whenNotFound_shouldReturn404() throws Exception {
        when(medicalRecordService.getById(recordId))
                .thenThrow(new ResourceNotFoundException("MedicalRecord", recordId));

        mockMvc.perform(get("/api/medical-records/{id}", recordId)
                        .with(authenticatedAs(vetUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getById_whenForbidden_shouldReturn403() throws Exception {
        when(medicalRecordService.getById(recordId)).thenReturn(record);
        org.mockito.Mockito.doThrow(new ForbiddenException("Du saknar behörighet"))
                .when(medicalRecordPolicy).canView(any(), any());

        mockMvc.perform(get("/api/medical-records/{id}", recordId)
                        .with(authenticatedAs(vetUser)))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    // GET /api/medical-records/my-records
    // -------------------------------------------------------------------------

    @Test
    void getMyRecords_asOwner_shouldReturn200WithList() throws Exception {
        when(medicalRecordService.getByOwner(any(UUID.class))).thenReturn(List.of(record));

        mockMvc.perform(get("/api/medical-records/my-records")
                        .with(authenticatedAs(ownerUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Halsont"));
    }

    @Test
    void getMyRecords_asVet_shouldReturn403() throws Exception {
        mockMvc.perform(get("/api/medical-records/my-records")
                        .with(authenticatedAs(vetUser)))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    // GET /api/medical-records/owner/{ownerId}
    // -------------------------------------------------------------------------

    @Test
    void getByOwner_shouldReturn200WithList() throws Exception {
        UUID ownerId = UUID.randomUUID();
        when(medicalRecordService.getByOwnerAllowedForUser(eq(ownerId), any(User.class)))
                .thenReturn(List.of(record));

        mockMvc.perform(get("/api/medical-records/owner/{ownerId}", ownerId)
                        .with(authenticatedAs(vetUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Halsont"));
    }

    @Test
    void getByOwner_whenOwnerRequestsOtherOwner_shouldReturn403() throws Exception {
        UUID otherOwnerId = UUID.randomUUID();

        mockMvc.perform(get("/api/medical-records/owner/{ownerId}", otherOwnerId)
                        .with(authenticatedAs(ownerUser)))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    // GET /api/medical-records/pet/{petId}
    // -------------------------------------------------------------------------

    @Test
    void getByPet_shouldReturn200WithList() throws Exception {
        when(medicalRecordService.getByPetAllowedForUser(eq(petId), any(User.class)))
                .thenReturn(List.of(record));

        mockMvc.perform(get("/api/medical-records/pet/{petId}", petId)
                        .with(authenticatedAs(vetUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Halsont"));
    }

    @Test
    void getByPet_whenNoRecords_shouldReturnEmptyList() throws Exception {
        when(medicalRecordService.getByPetAllowedForUser(eq(petId), any(User.class)))
                .thenReturn(List.of());

        mockMvc.perform(get("/api/medical-records/pet/{petId}", petId)
                        .with(authenticatedAs(vetUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // -------------------------------------------------------------------------
    // GET /api/medical-records/clinic/{clinicId}
    // -------------------------------------------------------------------------

    @Test
    void getByClinic_shouldReturn200WithList() throws Exception {
        when(medicalRecordService.getByClinic(clinicId)).thenReturn(List.of(record));

        mockMvc.perform(get("/api/medical-records/clinic/{clinicId}", clinicId)
                        .with(authenticatedAs(vetUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Halsont"));
    }

    @Test
    void getByClinic_whenForbidden_shouldReturn403() throws Exception {
        org.mockito.Mockito.doThrow(new ForbiddenException("Du saknar behörighet"))
                .when(medicalRecordPolicy).canViewClinic(any(), any());

        mockMvc.perform(get("/api/medical-records/clinic/{clinicId}", clinicId)
                        .with(authenticatedAs(vetUser)))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    // GET /api/medical-records/clinic/{clinicId}/status/{status}
    // -------------------------------------------------------------------------

    @Test
    void getByClinicAndStatus_shouldReturn200WithList() throws Exception {
        when(medicalRecordService.getByClinicAndStatus(clinicId, RecordStatus.OPEN))
                .thenReturn(List.of(record));

        mockMvc.perform(get("/api/medical-records/clinic/{clinicId}/status/{status}", clinicId, "OPEN")
                        .with(authenticatedAs(vetUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Halsont"));
    }

    @Test
    void getByClinicAndStatus_whenForbidden_shouldReturn403() throws Exception {
        org.mockito.Mockito.doThrow(new ForbiddenException("Du saknar behörighet"))
                .when(medicalRecordPolicy).canViewClinic(any(), any());

        mockMvc.perform(get("/api/medical-records/clinic/{clinicId}/status/{status}", clinicId, "OPEN")
                        .with(authenticatedAs(vetUser)))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    // PUT /api/medical-records/{id} — update
    // -------------------------------------------------------------------------

    @Test
    void update_shouldReturn200WithUpdatedRecord() throws Exception {
        record.setTitle("Ny titel");
        when(medicalRecordService.getById(recordId)).thenReturn(record);
        when(medicalRecordService.update(eq(recordId), eq("Ny titel"), eq("Ny beskrivning"), any(User.class)))
                .thenReturn(record);

        var request = new UpdateMedicalRecordRequest("Ny titel", "Ny beskrivning");

        mockMvc.perform(put("/api/medical-records/{id}", recordId)
                        .with(authenticatedAs(vetUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Ny titel"));
    }

    @Test
    void update_whenTitleIsBlank_shouldReturn400() throws Exception {
        var request = new UpdateMedicalRecordRequest("   ", "x");

        mockMvc.perform(put("/api/medical-records/{id}", recordId)
                        .with(authenticatedAs(vetUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void update_whenNotFound_shouldReturn404() throws Exception {
        when(medicalRecordService.getById(recordId))
                .thenThrow(new ResourceNotFoundException("MedicalRecord", recordId));

        var request = new UpdateMedicalRecordRequest("Ny titel", "Ny beskrivning");

        mockMvc.perform(put("/api/medical-records/{id}", recordId)
                        .with(authenticatedAs(vetUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void update_whenForbidden_shouldReturn403() throws Exception {
        when(medicalRecordService.getById(recordId)).thenReturn(record);
        org.mockito.Mockito.doThrow(new ForbiddenException("Du saknar behörighet"))
                .when(medicalRecordPolicy).canUpdate(any(), any());

        var request = new UpdateMedicalRecordRequest("Ny titel", "Ny beskrivning");

        mockMvc.perform(put("/api/medical-records/{id}", recordId)
                        .with(authenticatedAs(vetUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    // PUT /api/medical-records/{id}/assign-vet
    // -------------------------------------------------------------------------

    @Test
    void assignVet_shouldReturn200() throws Exception {
        UUID vetId = UUID.randomUUID();
        User vetToAssign = new User("Dr. Erik", "erik@vet.se", "hash", Role.VET);
        when(medicalRecordService.getById(recordId)).thenReturn(record);
        when(userService.getUserEntityById(vetId)).thenReturn(vetToAssign);
        when(medicalRecordService.assignVet(eq(recordId), eq(vetToAssign), any(User.class)))
                .thenReturn(record);

        var request = new AssignVetRequest(vetId);

        mockMvc.perform(put("/api/medical-records/{id}/assign-vet", recordId)
                        .with(authenticatedAs(vetUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Halsont"));
    }

    @Test
    void assignVet_whenVetIdIsNull_shouldReturn400() throws Exception {
        var request = new AssignVetRequest(null);

        mockMvc.perform(put("/api/medical-records/{id}/assign-vet", recordId)
                        .with(authenticatedAs(vetUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void assignVet_whenForbidden_shouldReturn403() throws Exception {
        UUID vetId = UUID.randomUUID();
        User vetToAssign = new User("Dr. Erik", "erik@vet.se", "hash", Role.VET);
        when(medicalRecordService.getById(recordId)).thenReturn(record);
        when(userService.getUserEntityById(vetId)).thenReturn(vetToAssign);
        org.mockito.Mockito.doThrow(new ForbiddenException("Du saknar behörighet"))
                .when(medicalRecordPolicy).canAssignVet(any(), any(), any());

        var request = new AssignVetRequest(vetId);

        mockMvc.perform(put("/api/medical-records/{id}/assign-vet", recordId)
                        .with(authenticatedAs(vetUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    // PUT /api/medical-records/{id}/status
    // -------------------------------------------------------------------------

    @Test
    void updateStatus_shouldReturn200() throws Exception {
        when(medicalRecordService.getById(recordId)).thenReturn(record);
        when(medicalRecordService.updateStatus(eq(recordId), eq(RecordStatus.AWAITING_INFO), any(User.class)))
                .thenReturn(record);

        var request = new UpdateStatusRequest(RecordStatus.AWAITING_INFO);

        mockMvc.perform(put("/api/medical-records/{id}/status", recordId)
                        .with(authenticatedAs(vetUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void updateStatus_whenStatusIsNull_shouldReturn400() throws Exception {
        var request = new UpdateStatusRequest(null);

        mockMvc.perform(put("/api/medical-records/{id}/status", recordId)
                        .with(authenticatedAs(vetUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStatus_whenForbidden_shouldReturn403() throws Exception {
        when(medicalRecordService.getById(recordId)).thenReturn(record);
        org.mockito.Mockito.doThrow(new ForbiddenException("Du saknar behörighet"))
                .when(medicalRecordPolicy).canUpdateStatus(any(), any(), any());

        var request = new UpdateStatusRequest(RecordStatus.AWAITING_INFO);

        mockMvc.perform(put("/api/medical-records/{id}/status", recordId)
                        .with(authenticatedAs(vetUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------------------
    // PUT /api/medical-records/{id}/close
    // -------------------------------------------------------------------------

    @Test
    void close_shouldReturn200() throws Exception {
        when(medicalRecordService.getById(recordId)).thenReturn(record);
        when(medicalRecordService.close(eq(recordId), any(User.class))).thenReturn(record);

        mockMvc.perform(put("/api/medical-records/{id}/close", recordId)
                        .with(authenticatedAs(vetUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Halsont"));
    }

    @Test
    void close_whenAlreadyClosed_shouldReturn409() throws Exception {
        when(medicalRecordService.getById(recordId)).thenReturn(record);
        when(medicalRecordService.close(eq(recordId), any(User.class)))
                .thenThrow(new BusinessRuleException("Ärendet är redan stängt"));

        mockMvc.perform(put("/api/medical-records/{id}/close", recordId)
                        .with(authenticatedAs(vetUser)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    void close_whenForbidden_shouldReturn403() throws Exception {
        when(medicalRecordService.getById(recordId)).thenReturn(record);
        org.mockito.Mockito.doThrow(new ForbiddenException("Du saknar behörighet"))
                .when(medicalRecordPolicy).canClose(any(), any());

        mockMvc.perform(put("/api/medical-records/{id}/close", recordId)
                        .with(authenticatedAs(vetUser)))
                .andExpect(status().isForbidden());
    }
}
