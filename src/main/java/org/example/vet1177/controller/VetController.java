package org.example.vet1177.controller;


import jakarta.validation.Valid;
import org.example.vet1177.dto.request.vet.VetRequest;
import org.example.vet1177.dto.response.vet.VetResponse;
import org.example.vet1177.entities.User;
import org.example.vet1177.policy.AdminPolicy;
import org.example.vet1177.services.VetService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/vets")
public class VetController {

    private static final Logger log = LoggerFactory.getLogger(VetController.class);

    private final VetService vetService;
    private final AdminPolicy adminPolicy;

    public VetController(VetService vetService, AdminPolicy adminPolicy) {
        this.vetService = vetService;
        this.adminPolicy = adminPolicy;
    }

    @PostMapping
    public ResponseEntity<VetResponse> createVet(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody VetRequest request) {
        log.info("POST /api/vets - creating vet");
        adminPolicy.requireAdmin(user);
        VetResponse response = vetService.createVet(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<VetResponse>> getAllVets() {
        log.info("GET /api/vets");
        List<VetResponse> response = vetService.getAllVets();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<VetResponse> getVetById(@PathVariable UUID id) {
        log.info("GET /api/vets/{}", id);
        VetResponse response = vetService.getVetById(id);
        return ResponseEntity.ok(response);
    }


}
