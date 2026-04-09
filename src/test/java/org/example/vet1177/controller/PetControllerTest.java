package org.example.vet1177.controller;
import tools.jackson.databind.ObjectMapper;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PetController.class)
@Import(SecurityConfig.class)
public class PetControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private PetService petService;
    @MockitoBean
    private UserService userService;

    private User owner;
    private User vet;
    private Pet pet;
    private UUID ownerId;
    private UUID petId;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        petId = UUID.randomUUID();
        owner = new User("Anna Ägare", "anna@mail.se", "hash", Role.OWNER);
        vet = new User("Dr. Erik Vet", "erik@vet.se", "hash", Role.VET);
        pet = new Pet(owner, "Molly", "Hund", "Labrador", LocalDate.of(2020, 1, 1), new BigDecimal("12.50"));
    }
    private RequestPostProcessor authenticatedAs(User user) {
        return authentication(new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities()
        ));
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

    //POST /pets
    @Test
    void createPet_shouldReturn200WithPetResponse() throws Exception {
        when(petService.createPet(any(), any(), any())).thenReturn(pet);

        mockMvc.perform(post("/pets")
                        .with(authenticatedAs(owner))
                        .header("currentUserId", ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPetRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Molly"));
    }

    @Test
    void createPet_whenInvalidRequest_shouldReturn400() throws Exception {
        PetRequest request = validPetRequest();
        request.setName("");

        mockMvc.perform(post("/pets")
                        .with(authenticatedAs(owner))
                        .header("currentUserId", ownerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void createPet_whenForbidden_shouldReturn403() throws Exception {
        when(petService.createPet(any(), any(), any()))
                .thenThrow(new ForbiddenException("Du kan inte lägga till ett djur"));

        mockMvc.perform(post("/pets")
                        .with(authenticatedAs(vet))
                        .header("currentUserId", UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validPetRequest())))
                .andExpect(status().isForbidden());
    }

}
