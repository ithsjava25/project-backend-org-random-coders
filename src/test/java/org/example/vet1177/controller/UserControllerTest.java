package org.example.vet1177.controller;

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
}