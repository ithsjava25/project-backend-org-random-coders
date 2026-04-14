package org.example.vet1177.security.auth.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Namn måste anges") String name,
        @NotBlank(message = "Email måste anges") @Email(message = "Ogiltig emailadress") String email,
        @NotBlank(message = "Lösenord måste anges") @Size(min = 8, message = "Lösenord måste vara minst 8 tecken") @JsonProperty(access = JsonProperty.Access.WRITE_ONLY) String password
) {
    @Override
    public String toString() {
        return "RegisterRequest[name=" + name + ", email=" + email + ", password=***]";
    }
}
