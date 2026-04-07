package org.example.vet1177.dto.response.user;

import org.example.vet1177.entities.Role;

import java.time.Instant;
import java.util.UUID;

public class UserResponse {

    private UUID id;
    private String name;
    private String email;
    private Role role;
    private UUID clinicId;
    private Instant createdAt;
    private Instant updatedAt;

    public UserResponse() {
    }

    public UserResponse(UUID id, String name, String email, Role role, UUID clinicId, Instant createdAt, Instant updatedAt) {
        this.id =id;
        this.name =name;
        this.email = email;
        this.role = role;
        this.clinicId = clinicId;
        this.createdAt = createdAt;
        this.updatedAt =updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public UUID getClinicId() {
        return clinicId;
    }

    public void setClinicId(UUID clinicId) {
        this.clinicId = clinicId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
