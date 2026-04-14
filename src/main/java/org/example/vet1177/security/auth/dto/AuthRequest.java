package org.example.vet1177.security.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;


public record AuthRequest(
        @NotBlank(message = "Email måste anges") @Email(message = "Ogiltig emailadress") String email,
        @NotBlank(message = "Lösenord måste anges") @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String password
) {
    @Override
    public String toString() {
        return "AuthRequest[email=" + email + ", password=***]";
    }
}
