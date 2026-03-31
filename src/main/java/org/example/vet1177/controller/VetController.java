package org.example.vet1177.controller;


import jakarta.validation.Valid;
import org.example.vet1177.dto.request.vet.VetRequest;
import org.example.vet1177.dto.response.vet.VetResponse;
import org.example.vet1177.services.VetService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vets")
public class VetController {

    private final VetService vetService;

    public VetController(VetService vetService) {
        this.vetService = vetService;
    }

    @PostMapping
    public ResponseEntity<VetResponse> createVet(@Valid @RequestBody VetRequest request) {
        VetResponse response = vetService.createVet(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<VetResponse>> getAllVets() {
        List<VetResponse> response = vetService.getAllVets();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VetResponse> getVetById(@PathVariable UUID id) {
        VetResponse response = vetService.getVetById(id);
        return ResponseEntity.ok(response);
    }


}
