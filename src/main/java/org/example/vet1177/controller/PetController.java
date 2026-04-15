package org.example.vet1177.controller;

import jakarta.validation.Valid;
import org.example.vet1177.dto.request.pet.PetRequest;
import org.example.vet1177.dto.response.pet.PetResponse;
import org.example.vet1177.entities.Pet;
import org.example.vet1177.entities.User;
import org.example.vet1177.services.PetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pets")
public class PetController {

    private static final Logger log = LoggerFactory.getLogger(PetController.class);

    private final PetService petService;

    public PetController(PetService petService) {
        this.petService = petService;
    }

    //POST /pets - skapa nytt djur
    @PostMapping
    public PetResponse createPet(
            @AuthenticationPrincipal User currentUser,
            @RequestParam(required = false) UUID ownerId,
            @Valid @RequestBody PetRequest request
    ) {
        log.info("POST /pets - creating pet for userId={} ownerId={}", currentUser.getId(), ownerId);
        Pet saved = petService.createPet(currentUser.getId(), ownerId, request);
        return toResponse(saved);
    }

    // GET /pets/{petId} - hämta ett specifikt djur
    @GetMapping("/{petId}")
    public ResponseEntity<PetResponse> getPetById(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID petId
    ) {
        log.info("GET /pets/{}", petId);
        Pet pet = petService.getPetById(petId, currentUser);
        return ResponseEntity.ok(toResponse(pet));
    }

    // GET /pets/owner/{ownerId} - hämta alla djur för en ägare
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<PetResponse>> getPetsByOwner(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID ownerId
    ) {
        log.info("GET /pets/owner/{}", ownerId);
        List<PetResponse> pets = petService.getPetsByOwner(currentUser.getId(), ownerId)
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(pets);
    }

    // PUT /pets/{petId} - uppdatera ett djur
    @PutMapping("/{petId}")
    public ResponseEntity<PetResponse> updatePet(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID petId,
            @Valid @RequestBody PetRequest request
    ) {
        log.info("PUT /pets/{}", petId);
        Pet updated = petService.updatePet(currentUser.getId(), petId, request);
        return ResponseEntity.ok(toResponse(updated));
    }

    // DELETE /pets/{petId} - radera ett djur
    @DeleteMapping("/{petId}")
    public ResponseEntity<Void> deletePet(
            @AuthenticationPrincipal User currentUser,
            @PathVariable UUID petId
    ) {
        log.info("DELETE /pets/{}", petId);
        petService.deletePet(petId, currentUser);
        return ResponseEntity.noContent().build();
    }

    //Helper
    private PetResponse toResponse(Pet pet) {
        return new PetResponse(
                pet.getId(),
                pet.getOwner().getId(),
                pet.getName(),
                pet.getSpecies(),
                pet.getBreed(),
                pet.getDateOfBirth(),
                pet.getWeightKg(),
                pet.getCreatedAt(),
                pet.getUpdatedAt()
        );
    }

}
