package org.example.vet1177.controller;

import org.example.vet1177.dto.request.user.UserUpdateRequest;
import org.example.vet1177.exception.BusinessRuleException;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import tools.jackson.databind.ObjectMapper;
import org.example.vet1177.dto.request.user.UserRequest;

import org.example.vet1177.dto.response.user.UserResponse;
import org.example.vet1177.entities.Role;
import org.example.vet1177.entities.User;

import org.example.vet1177.exception.ResourceNotFoundException;
import org.example.vet1177.security.SecurityConfig;
import org.example.vet1177.services.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    private User currentUser;
    private UserResponse userResponse;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        currentUser = new User("Anna Karlsson", "anna@example.se", "hash", Role.ADMIN);
        userResponse = new UserResponse(userId, "Anna Karlsson", "anna@example.se", Role.OWNER, null, null, null);
    }

    private RequestPostProcessor authenticatedAs(User user) {
        return authentication(new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities()
        ));
    }

    private UserRequest validUserRequest() {
        UserRequest request = new UserRequest();
        request.setName("Anna Karlsson");
        request.setEmail("anna@example.se");
        request.setPassword("lösenord123");
        request.setRole(Role.OWNER);
        return request;
    }


    // GET /api/users

    @Test
    void getAllUsers_shouldReturn200WithList() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(userResponse));

        mockMvc.perform(get("/api/users")
                        .with(authenticatedAs(currentUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Anna Karlsson"));
    }

    @Test
    void getAllUsers_whenEmpty_shouldReturnEmptyList() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of());

        mockMvc.perform(get("/api/users")
                        .with(authenticatedAs(currentUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // GET /api/users/{id}

    @Test
    void getUserById_shouldReturn200WithUserResponse() throws Exception {
        when(userService.getById(userId)).thenReturn(userResponse);

        mockMvc.perform(get("/api/users/{id}", userId)
                        .with(authenticatedAs(currentUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Anna Karlsson"));
    }

    @Test
    void getUserById_whenNotFound_shouldReturn404() throws Exception {
        when(userService.getById(any()))
                .thenThrow(new ResourceNotFoundException("User", userId));

        mockMvc.perform(get("/api/users/{id}", userId)
                        .with(authenticatedAs(currentUser)))
                .andExpect(status().isNotFound());
    }

    // POST /api/users
    @Test
    void createUser_shouldReturn201WithUserResponse() throws Exception {
        when(userService.createUser(any())).thenReturn(userResponse);

        mockMvc.perform(post("/api/users")
                        .with(authenticatedAs(currentUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Anna Karlsson"));
    }

    @Test
    void createUser_whenInvalidRequest_shouldReturn400() throws Exception {
        UserRequest request = validUserRequest();
        request.setEmail("inte-en-email");

        mockMvc.perform(post("/api/users")
                        .with(authenticatedAs(currentUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
        verify(userService, never()).createUser(any());
    }

    @Test
    void createUser_whenEmailAlreadyTaken_shouldReturn400() throws Exception {
        when(userService.createUser(any()))
                .thenThrow(new BusinessRuleException("Email används redan"));

        mockMvc.perform(post("/api/users")
                        .with(authenticatedAs(currentUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUserRequest())))
                .andExpect(status().isBadRequest());
    }


    // PUT /api/users/{id}


    @Test
    void updateUser_shouldReturn200WithUpdatedUserResponse() throws Exception {
        UserResponse updated = new UserResponse(userId, "Uppdaterad Namn", "anna@example.se", Role.OWNER, null, null, null);
        when(userService.updateUser(eq(userId), any())).thenReturn(updated);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("Uppdaterad Namn");

        mockMvc.perform(put("/api/users/{id}", userId)
                        .with(authenticatedAs(currentUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Uppdaterad Namn"));
    }

    @Test
    void updateUser_whenNotFound_shouldReturn404() throws Exception {
        when(userService.updateUser(any(), any()))
                .thenThrow(new ResourceNotFoundException("User", userId));

        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("Uppdaterad Namn");

        mockMvc.perform(put("/api/users/{id}", userId)
                        .with(authenticatedAs(currentUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateUser_whenBusinessRuleViolation_shouldReturn400() throws Exception {
        when(userService.updateUser(any(), any()))
                .thenThrow(new BusinessRuleException("Email används redan"));

        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("tagen@example.se");

        mockMvc.perform(put("/api/users/{id}", userId)
                        .with(authenticatedAs(currentUser))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    // DELETE /api/users/{id}

    @Test
    void deleteUser_shouldReturn204() throws Exception {
        doNothing().when(userService).deleteUser(userId);

        mockMvc.perform(delete("/api/users/{id}", userId)
                        .with(authenticatedAs(currentUser)))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_whenNotFound_shouldReturn404() throws Exception {
        doThrow(new ResourceNotFoundException("User", userId))
                .when(userService).deleteUser(any());

        mockMvc.perform(delete("/api/users/{id}", userId)
                        .with(authenticatedAs(currentUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_whenHasLinkedResources_shouldReturn400() throws Exception {
        doThrow(new BusinessRuleException("Användaren har kopplade djur och kan inte raderas"))
                .when(userService).deleteUser(any());

        mockMvc.perform(delete("/api/users/{id}", userId)
                        .with(authenticatedAs(currentUser)))
                .andExpect(status().isBadRequest());
    }
}

