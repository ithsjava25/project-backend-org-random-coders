package org.example.vet1177.security.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Namn måste anges") String name,
        @NotBlank(message = "Email måste anges") @Email(message = "Ogiltig emailadress") String email,
        @NotBlank(message = "Lösenord måste anges") @Size(min = 8, message = "Lösenord måste vara minst 8 tecken") String password
) {}
