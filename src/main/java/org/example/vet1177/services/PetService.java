package org.example.vet1177.services;

import org.example.vet1177.entities.Pet;
import org.example.vet1177.repository.PetRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class PetService {

    private final PetRepository petRepository;

    public PetService(PetRepository petRepository) {
        this.petRepository = petRepository;
    }

    public Pet createPet(Pet pet) {
        return petRepository.save(pet);
    }

    public List<Pet> getAllPets() {
        return petRepository.findAll();
    }

    public Pet getPetById(UUID id) {
        return petRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Husdjur hittades inte"));
    }

    public List<Pet> getPetsByOwner(UUID ownerId) {
        return petRepository.findByOwnerId(ownerId);
    }
}