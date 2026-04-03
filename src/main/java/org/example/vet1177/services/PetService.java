package org.example.vet1177.services;

import jakarta.transaction.Transactional;
import org.example.vet1177.entities.Pet;
import org.example.vet1177.entities.Role;
import org.example.vet1177.entities.User;
import org.example.vet1177.policy.PetPolicy;
import org.example.vet1177.repository.MedicalRecordRepository;
import org.example.vet1177.repository.PetRepository;
import org.example.vet1177.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public class PetService {


    private final PetRepository petRepository;
    private final UserRepository userRepository;
    private final PetPolicy petPolicy;
    private final MedicalRecordRepository medicalRecordRepository;


    public PetService(PetRepository petRepository,
                      UserRepository userRepository, PetPolicy petPolicy,
                      MedicalRecordRepository medicalRecordRepository) {
        this.petRepository = petRepository;
        this.userRepository = userRepository;
        this.petPolicy = petPolicy;
        this.medicalRecordRepository = medicalRecordRepository;
    }

    // CREATE - Måste vara en OWNER för att få skapa/lägga till ett djur.
    public Pet createPet(UUID currentUserId, UUID ownerId, Pet pet) {
        User currentUser = getUserById(currentUserId);

        if (!petPolicy.canCreate(currentUser)) {
            throw new RuntimeException("Du kan inte lägga till ett djur");
        }

        User owner;

        if (currentUser.getRole() == Role.ADMIN) {
            if (ownerId == null) {
                throw new RuntimeException("Admin måste ange ownerId");
            }
            owner = getUserById(ownerId);
            if (owner.getRole() != Role.OWNER) {
                throw new RuntimeException("ownerId måste tillhöra en användare med rollen OWNER");
            }
        } else {
            owner = currentUser;
        }

        pet.setOwner(owner);
        return petRepository.save(pet);
    }

    // READ
    // - Måste vara ADMIN eller OWNER för att se pet
    public Pet getPetById(UUID petId, User currentUser) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Pet not found"));

        if (petPolicy.canView(currentUser, pet)) {
            return pet;
        }

        if (currentUser.getRole() == Role.VET) {
            if (currentUser.getClinic() == null) {
                throw new RuntimeException("Veterinären saknar koppling till klinik");
            }
            boolean vetHasAccess = medicalRecordRepository
                    .existsByPetIdAndClinicId(petId, currentUser.getClinic().getId());

            if (vetHasAccess) {
                return pet;
            }
        }

        throw new RuntimeException("Du har inte behörighet att se detta djur");
    }

    // Lista djur som tillhör ägaren
    public List<Pet> getPetsByOwner(UUID currentUserId, UUID ownerId) {
        User currentUser = getUserById(currentUserId);
        if (!petPolicy.canViewOwnerPets(currentUser, ownerId)){
            throw new RuntimeException("Du saknar behörighet");
        }
        return petRepository.findByOwnerId(ownerId);
    }

    // UPDATE
    public Pet updatePet(UUID currentUserId, UUID petId, Pet updatedPet) {
        User currentUser = getUserById(currentUserId);
        Pet existingPet = getPetByIdOrThrow(petId);

        if (!petPolicy.canUpdate(currentUser, existingPet)) {
            throw new RuntimeException("Du saknar behörighet för att uppdatera info om djuret");
        }

        existingPet.setName(updatedPet.getName());
        existingPet.setSpecies(updatedPet.getSpecies());
        existingPet.setBreed(updatedPet.getBreed());
        existingPet.setDateOfBirth(updatedPet.getDateOfBirth());
        existingPet.setWeightKg(updatedPet.getWeightKg());


        return petRepository.save(existingPet);
    }

    // DELETE
    @Transactional
    public void deletePet(UUID petId, User currentUser) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new RuntimeException("Djuret finns inte"));

        if (!petPolicy.canDelete(currentUser, pet)) {
            throw new RuntimeException("Du har inte behörighet att radera detta djur");
        }
        try {
            petRepository.delete(pet);
            petRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new RuntimeException("Djuret kan inte raderas eftersom journaler finns kopplade");
        }
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