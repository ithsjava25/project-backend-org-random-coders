package org.example.vet1177.security.auth.dto;

import org.example.vet1177.entities.Role;

import java.util.UUID;

public record AuthResponse(
        String token,
        UUID userId,
        String name,
        String email,
        Role role
) {}
