package org.example.vet1177.repository;

import org.example.vet1177.entities.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PetRepository extends JpaRepository<Pet, UUID> {

    // Hitta djur via ägare
    List<Pet> findByOwnerId(UUID ownerId);
}