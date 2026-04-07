package org.example.vet1177.controller;

import jakarta.validation.Valid;
import org.example.vet1177.dto.request.pet.PetRequest;
import org.example.vet1177.dto.response.pet.PetResponse;
import org.example.vet1177.entities.Pet;
import org.example.vet1177.entities.User;
import org.example.vet1177.services.PetService;
import org.example.vet1177.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/pets")
public class PetController {

    private final PetService petService;
    private final UserService userService;

    public PetController(PetService petService, UserService userService) {
        this.petService = petService;
        this.userService = userService;
    }

    //POST / pets - skapa nytt djur
    @PostMapping
    public PetResponse createPet(
            // TODO: Ersätt med användare från autentiserad kontext (t.ex. JWT / Spring Security)
            @RequestHeader UUID currentUserId,
            @RequestParam(required = false) UUID ownerId,
            @Valid @RequestBody PetRequest request
    ) {
        Pet saved = petService.createPet(currentUserId, ownerId, request);
        return toResponse(saved);
    }

    // GET / pets/{petId} - hämta ett specifikt djur
    @GetMapping("/{petId}")
    public ResponseEntity<PetResponse> getPetById(
            @RequestHeader UUID currentUserId,
            @PathVariable UUID petId
    ) {
        User currentUser = userService.getUserEntityById(currentUserId);
        Pet pet = petService.getPetById(petId, currentUser);
        return ResponseEntity.ok(toResponse(pet));
    }

    // GET/pets/owner/{ownerId} - hämta alla djur för en ägare
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<List<PetResponse>> getPetsByOwner(
            @RequestHeader UUID currentUserId,
            @PathVariable UUID ownerId
    ) {
        List<PetResponse> pets = petService.getPetsByOwner(currentUserId, ownerId)
                .stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(pets);
    }

    // PUT /pets/{petId} - uppdatera ett djur
    @PutMapping("/{petId}")
    public ResponseEntity<PetResponse> updatePet(
            @RequestHeader UUID currentUserId,
            @PathVariable UUID petId,
            @Valid @RequestBody PetRequest request
    ) {
        Pet updated = petService.updatePet(currentUserId, petId, request);
        return ResponseEntity.ok(toResponse(updated));
    }

    // DELETE /pets/{petId} - radera ett djur
    @DeleteMapping("/{petId}")
    public ResponseEntity<Void> deletePet(
            @RequestHeader UUID currentUserId,
            @PathVariable UUID petId
    ) {
        User currentUser = userService.getUserEntityById(currentUserId);
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