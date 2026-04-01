package org.example.vet1177.controller;

import jakarta.validation.Valid;
import org.example.vet1177.dto.request.pet.PetRequest;
import org.example.vet1177.dto.response.pet.PetResponse;
import org.example.vet1177.entities.Pet;
import org.example.vet1177.services.PetService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/pets")
public class PetController {

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @PostMapping
    public PetResponse createPet(
            @RequestParam UUID currentUserId,
            @RequestParam(required = false) UUID ownerId,
            @Valid @RequestBody PetRequest request
    ) {
        Pet pet = new Pet();
        pet.setName(request.getName());
        pet.setSpecies(request.getSpecies());
        pet.setBreed(request.getBreed());
        pet.setDateOfBirth(request.getDateOfBirth());
        pet.setWeightKg(request.getWeightKg());
        pet.setInsuranceNumber(request.getInsuranceNumber());

        Pet saved = petService.createPet(currentUserId, ownerId, pet);

        return new PetResponse(
                saved.getId(),
                saved.getOwner().getId(),
                saved.getName(),
                saved.getSpecies(),
                saved.getBreed(),
                saved.getDateOfBirth(),
                saved.getWeightKg(),
                saved.getInsuranceNumber(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }
}