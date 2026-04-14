package org.example.vet1177.security.auth.dto;

import jakarta.validation.constraints.NotBlank;

public record AuthRequest(
        @NotBlank(message = "Email måste anges") String email,
        @NotBlank(message = "Lösenord måste anges") String password
) {}
