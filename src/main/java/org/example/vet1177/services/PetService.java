package org.example.vet1177.services;

import org.example.vet1177.entities.Pet;
import org.example.vet1177.entities.User;
import org.example.vet1177.policy.PetPolicy;
import org.example.vet1177.repository.PetRepository;
import org.example.vet1177.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public class PetService {


    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final PetPolicy petPolicy;


    public PetService(PetRepository petRepository,
                      UserRepository userRepository,
                      PetPolicy petPolicy) {
        this.petRepository = petRepository;
        this.userRepository = userRepository;
        this.petPolicy = petPolicy;
    }

    // CREATE - Måste vara en OWNER för att få skapa/lägga till ett djur.
        public Pet createPet(UUID currentUserId, Pet pet) {
            User currentUser = getUserById(currentUserId);

            if (!petPolicy.canCreate(currentUser)) {
                throw new RuntimeException("Du kan inte lägga till ett djur");
            }

            pet.setOwner(currentUser);
            return petRepository.save(pet);
        }

    // READ
    // - Måste vara ADMIN eller OWNER för att se pet
    public Pet getPetById(UUID currentUserId, UUID petId) {
        User currentUser = getUserById(currentUserId);
        Pet pet = getPetByIdOrThrow(petId);

        if(!petPolicy.canView(currentUser, pet)){
            throw new RuntimeException("Du har inte behörighet till djuret du söker");
        }
        return pet;
    }

    public Pet getPetById(UUID id) {
        return petRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Husdjur hittades inte"));
    }

    public List<Pet> getPetsByOwner(UUID ownerId) {
        return petRepository.findByOwnerId(ownerId);
    }

    //HELPERS
    private User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(()-> new RuntimeException("Användare ej hittad"));
    }
    private Pet getPetByIdOrThrow(UUID petId) {
        return petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Djuret ej hittat"));
}

}