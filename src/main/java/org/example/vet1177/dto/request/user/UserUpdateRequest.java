package org.example.vet1177.dto.request.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import org.example.vet1177.entities.Role;

import java.util.UUID;

public class UserUpdateRequest {

    @Size(min = 2, max = 100)
    private String name;

    @Email
    private String email;

    private UUID clinicId;

    private Role role;

    public UUID getClinicId() {
        return clinicId;
    }

    public void setClinicId(UUID clinicId) {
        this.clinicId = clinicId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}