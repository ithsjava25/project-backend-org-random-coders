package org.example.vet1177.services;

import jakarta.transaction.Transactional;
import org.example.vet1177.dto.request.pet.PetRequest;
import org.example.vet1177.entities.Pet;
import org.example.vet1177.entities.Role;
import org.example.vet1177.entities.User;
import org.example.vet1177.exception.BusinessRuleException;
import org.example.vet1177.exception.ForbiddenException;
import org.example.vet1177.exception.ResourceNotFoundException;
import org.example.vet1177.policy.PetPolicy;
import org.example.vet1177.repository.MedicalRecordRepository;
import org.example.vet1177.repository.PetRepository;
import org.example.vet1177.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;


@Service
public class PetService {

    private static final Logger log = LoggerFactory.getLogger(PetService.class);

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

    // CREATE - OWNER kan skapa för sig själv, ADMIN kan skapa för en OWNER via ownerId.
    public Pet createPet(UUID currentUserId, UUID ownerId, PetRequest request) {
        log.info("Creating pet currentUserId={} ownerId={}", currentUserId, ownerId);
        User currentUser = getUserById(currentUserId);

        if (!petPolicy.canCreate(currentUser)) {
            throw new ForbiddenException("Du kan inte lägga till ett djur");
        }
        User owner;

        if (currentUser.getRole() == Role.ADMIN) {
            if (ownerId == null) {
                throw new BusinessRuleException("Admin måste ange ownerId");
            }
            owner = getUserById(ownerId);
            if (owner.getRole() != Role.OWNER) {
                throw new BusinessRuleException("ownerId måste tillhöra en användare med rollen OWNER");
            }
        } else {
            owner = currentUser;
        }

        Pet pet = new Pet();
        applyPetRequest(pet, request);
        pet.setOwner(owner);

        return petRepository.save(pet);
    }

    // READ
    // - Måste vara ADMIN eller OWNER för att se pet
    public Pet getPetById(UUID petId, User currentUser) {
        log.debug("Fetching pet id={}", petId);
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet", petId));

        if (petPolicy.canView(currentUser, pet)) {
            return pet;
        }

        if (currentUser.getRole() == Role.VET) {
            if (currentUser.getClinic() == null) {
                throw new ForbiddenException("Veterinären saknar koppling till klinik");
            }
            boolean vetHasAccess = medicalRecordRepository
                    .existsByPetIdAndClinicId(petId, currentUser.getClinic().getId());

            if (vetHasAccess) {
                return pet;
            }
        }

        throw new ForbiddenException("Du har inte behörighet att se detta djur");
    }

    // Lista djur som tillhör ägaren
    public List<Pet> getPetsByOwner(UUID currentUserId, UUID ownerId) {
        log.debug("Fetching pets ownerId={}", ownerId);
        User currentUser = getUserById(currentUserId);
        if (!petPolicy.canViewOwnerPets(currentUser, ownerId)){
            throw new ForbiddenException("Du saknar behörighet");
        }
        return petRepository.findByOwnerId(ownerId);
    }

    // UPDATE
    public Pet updatePet(UUID currentUserId, UUID petId, PetRequest request) {
        log.info("Updating pet id={}", petId);
        User currentUser = getUserById(currentUserId);
        Pet existingPet = getPetByIdOrThrow(petId);

        if (!petPolicy.canUpdate(currentUser, existingPet)) {
            throw new ForbiddenException("Du saknar behörighet för att uppdatera info om djuret");
        }

        existingPet.setName(request.getName());
        existingPet.setSpecies(request.getSpecies());
        existingPet.setBreed(request.getBreed());
        existingPet.setDateOfBirth(request.getDateOfBirth());
        existingPet.setWeightKg(request.getWeightKg());


        return petRepository.save(existingPet);
    }

    // DELETE
    @Transactional
    public void deletePet(UUID petId, User currentUser) {
        log.info("Deleting pet id={}", petId);
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet", petId));

        if (!petPolicy.canDelete(currentUser, pet)) {
            throw new ForbiddenException("Du har inte behörighet att radera detta djur");
        }
        try {
            petRepository.delete(pet);
            petRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new BusinessRuleException("Djuret kan inte raderas eftersom journaler finns kopplade");
        }
    }



    //HELPERS
    private User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(()-> new ResourceNotFoundException("User", userId));
    }
    private Pet getPetByIdOrThrow(UUID petId) {
        return petRepository.findById(petId)
                .orElseThrow(() -> new ResourceNotFoundException("Pet", petId));
}

    private void applyPetRequest(Pet target, PetRequest request) {
        target.setName(request.getName());
        target.setSpecies(request.getSpecies());
        target.setBreed(request.getBreed());
        target.setDateOfBirth(request.getDateOfBirth());
        target.setWeightKg(request.getWeightKg());
    }

}