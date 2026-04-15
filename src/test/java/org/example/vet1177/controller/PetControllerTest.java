package org.example.vet1177.controller;

//Används i commentControllerTest också, verkar funka där?
import org.example.vet1177.security.CustomUserDetailsService;
import org.example.vet1177.security.JwtService;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.ObjectMapper;
import org.example.vet1177.exception.ResourceNotFoundException;
import org.example.vet1177.dto.request.pet.PetRequest;
import org.example.vet1177.entities.Pet;
import org.example.vet1177.entities.Role;
import org.example.vet1177.entities.User;
import org.example.vet1177.exception.ForbiddenException;
import org.example.vet1177.security.SecurityConfig;
import org.example.vet1177.services.PetService;
import org.example.vet1177.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PetController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
public class PetControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private PetService petService;
    @MockitoBean
    private UserService userService;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtService jwtService;

    private User owner;
    private User vet;
    private Pet pet;
    private UUID ownerId;
    private UUID petId;



    @BeforeEach
    void setUp() throws Exception {
        ownerId = UUID.randomUUID();
        petId = UUID.randomUUID();
        owner = new User("Anna Ägare", "anna@mail.se", "hash", Role.OWNER);
        vet = new User("Dr. Erik Vet", "erik@vet.se", "hash", Role.VET);
        pet = new Pet(owner, "Molly", "Hund", "Labrador", LocalDate.of(2020, 1, 1), new BigDecimal("12.50"));
    }

    private PetRequest validPetRequest() {
        PetRequest request = new PetRequest();
        request.setName("Molly");
        request.setSpecies("Hund");
        request.setBreed("Labrador");
        request.setDateOfBirth(LocalDate.of(2020, 1, 1));
        request.setWeightKg(new BigDecimal("12.50"));
        return request;
    }

    private RequestPostProcessor authenticatedAs(User user) {
        return authentication(new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities()
        ));
    }

    //POST /pets
    @Test
    void createPet_shouldReturn200WithPetResponse() throws Exception {
        when(petService.createPet(any(), any(), any())).thenReturn(pet);

        mockMvc.perform(post("/api/pets")
                        .with(authenticatedAs(owner))

                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPetRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Molly"));
    }

    @Test
    void createPet_whenInvalidRequest_shouldReturn400() throws Exception {
        PetRequest request = validPetRequest();
        request.setName("");

        mockMvc.perform(post("/api/pets")
                        .with(authenticatedAs(owner))

                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPet_whenForbidden_shouldReturn403() throws Exception {
        when(petService.createPet(any(), any(), any()))
                .thenThrow(new ForbiddenException("Du kan inte lägga till ett djur"));

        mockMvc.perform(post("/api/pets")
                        .with(authenticatedAs(vet))

                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPetRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    void createPet_whenUnexpectedError_shouldReturn500() throws Exception {
        when(petService.createPet(any(), any(), any()))
                .thenThrow(new RuntimeException("Unexpected failure"));

        mockMvc.perform(post("/api/pets")
                        .with(authenticatedAs(owner))

                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPetRequest())))
                .andExpect(status().isInternalServerError());
    }
    // GET /pets/{petId}

    @Test
    void getPetById_shouldReturn200WithPetResponse() throws Exception {
        when(petService.getPetById(any(), any())).thenReturn(pet);

        mockMvc.perform(get("/api/pets/{petId}", petId)
                        .with(authenticatedAs(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Molly"));
    }

    @Test
    void getPetById_whenNotFound_shouldReturn404() throws Exception {
        when(petService.getPetById(any(), any()))
                .thenThrow(new ResourceNotFoundException("Pet", petId));

        mockMvc.perform(get("/api/pets/{petId}", petId)
                        .with(authenticatedAs(owner)))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPetById_whenForbidden_shouldReturn403() throws Exception {
        when(petService.getPetById(any(), any()))
                .thenThrow(new ForbiddenException("Du har inte behörighet att se detta djur"));

        mockMvc.perform(get("/api/pets/{petId}", petId)
                        .with(authenticatedAs(vet)))
                .andExpect(status().isForbidden());
    }
    // GET /pets/owner/{ownerId}

    @Test
    void getPetsByOwner_shouldReturn200WithList() throws Exception {
        when(petService.getPetsByOwner(any(), any())).thenReturn(List.of(pet));

        mockMvc.perform(get("/api/pets/owner/{ownerId}", ownerId)
                        .with(authenticatedAs(owner)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Molly"));
    }

    @Test
    void getPetsByOwner_whenForbidden_shouldReturn403() throws Exception {
        when(petService.getPetsByOwner(any(), any()))
                .thenThrow(new ForbiddenException("Du saknar behörighet"));

        mockMvc.perform(get("/api/pets/owner/{ownerId}", ownerId)
                        .with(authenticatedAs(vet)))
                .andExpect(status().isForbidden());
    }

    // PUT /pets/{petId}

    @Test
    void updatePet_shouldReturn200WithUpdatedPetResponse() throws Exception {
        Pet updated = new Pet(owner, "Harry", "Katt", "Siamese", LocalDate.of(2021, 3, 5), new BigDecimal("5.00"));
        when(petService.updatePet(any(), any(), any())).thenReturn(updated);

        mockMvc.perform(put("/api/pets/{petId}", petId)
                        .with(authenticatedAs(owner))

                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPetRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Harry"));
    }

    @Test
    void updatePet_whenNotFound_shouldReturn404() throws Exception {
        when(petService.updatePet(any(), any(), any()))
                .thenThrow(new ResourceNotFoundException("Pet", petId));

        mockMvc.perform(put("/api/pets/{petId}", petId)
                        .with(authenticatedAs(owner))

                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPetRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    void updatePet_whenForbidden_shouldReturn403() throws Exception {
        when(petService.updatePet(any(), any(), any()))
                .thenThrow(new ForbiddenException("Du saknar behörighet"));

        mockMvc.perform(put("/api/pets/{petId}", petId)
                        .with(authenticatedAs(vet))

                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPetRequest())))
                .andExpect(status().isForbidden());
    }

    //DELETE /pets/{petId}


    @Test
    void deletePet_shouldReturn204() throws Exception {
        doNothing().when(petService).deletePet(any(), any());

        mockMvc.perform(delete("/api/pets/{petId}", petId)
                        .with(authenticatedAs(owner)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deletePet_whenNotFound_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("Pet", petId))
                .when(petService).deletePet(any(), any());

        mockMvc.perform(delete("/api/pets/{petId}", petId)
                        .with(authenticatedAs(owner)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deletePet_whenForbidden_shouldReturn403() throws Exception {
        doThrow(new ForbiddenException("Du har inte behörighet att radera detta djur"))
                .when(petService).deletePet(any(), any());

        mockMvc.perform(delete("/api/pets/{petId}", petId)
                        .with(authenticatedAs(vet)))
                .andExpect(status().isForbidden());
    }

}
