package org.example.vet1177.controller;

import jakarta.validation.Valid;
import org.example.vet1177.dto.request.pet.PetRequest;
import org.example.vet1177.dto.response.pet.PetResponse;
import org.example.vet1177.entities.Pet;
import org.example.vet1177.services.PetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/pets")
public class PetController {

    private static final Logger log = LoggerFactory.getLogger(PetController.class);

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    @PostMapping
    public PetResponse createPet(
            // TODO: Ersätt med användare från autentiserad kontext (t.ex. JWT / Spring Security)
            @RequestHeader UUID currentUserId,
            @RequestParam(required = false) UUID ownerId,
            @Valid @RequestBody PetRequest request
    ) {
        log.info("POST /pets - creating pet for currentUserId={} ownerId={}", currentUserId, ownerId);
        Pet saved = petService.createPet(currentUserId, ownerId, request);

        return new PetResponse(
                saved.getId(),
                saved.getOwner().getId(),
                saved.getName(),
                saved.getSpecies(),
                saved.getBreed(),
                saved.getDateOfBirth(),
                saved.getWeightKg(),
                saved.getCreatedAt(),
                saved.getUpdatedAt()
        );
    }
}