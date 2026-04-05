package org.example.vet1177.dto.response.pet;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class PetResponse {

    private UUID id;
    private UUID ownerId;
    private String name;
    private String species;
    private String breed;
    private LocalDate dateOfBirth;
    private BigDecimal weightKg;
    private Instant createdAt;
    private Instant updatedAt;

    public PetResponse() {
    }

    public PetResponse(UUID id, UUID ownerId, String name, String species,
                       String breed, LocalDate dateOfBirth, BigDecimal weightKg,
                       Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.name = name;
        this.species = species;
        this.breed = breed;
        this.dateOfBirth = dateOfBirth;
        this.weightKg = weightKg;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public String getName() {
        return name;
    }

    public String getSpecies() {
        return species;
    }

    public String getBreed() {
        return breed;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}