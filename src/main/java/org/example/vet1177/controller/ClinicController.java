package org.example.vet1177.controller;

import jakarta.validation.Valid;
import org.example.vet1177.dto.request.clinic.CreateClinicRequest;
import org.example.vet1177.dto.request.clinic.UpdateClinicRequest;
import org.example.vet1177.dto.response.clinic.ClinicResponse;
import org.example.vet1177.services.ClinicService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clinics")
public class ClinicController {

    private final ClinicService clinicService;

    public ClinicController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<ClinicResponse> create(
            @Valid @RequestBody CreateClinicRequest request) {

        return ResponseEntity.ok(
                ClinicResponse.from(
                        clinicService.create(
                                request.name(),
                                request.address(),
                                request.phoneNumber()
                        )
                )
        );
    }

    // READ ALL
    @GetMapping
    public ResponseEntity<List<ClinicResponse>> getAll() {
        return ResponseEntity.ok(
                clinicService.getAll()
                        .stream()
                        .map(ClinicResponse::from)
                        .toList()
        );
    }

    // READ ONE
    @GetMapping("/{id}")
    public ResponseEntity<ClinicResponse> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(
                ClinicResponse.from(
                        clinicService.getById(id)
                )
        );
    }

    // UPDATE
    @PutMapping("/{id}")
    public ResponseEntity<ClinicResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateClinicRequest request) {

        return ResponseEntity.ok(
                ClinicResponse.from(
                        clinicService.update(
                                id,
                                request.name(),
                                request.address(),
                                request.phoneNumber()
                        )
                )
        );
    }

    // DELETE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        clinicService.delete(id);
        return ResponseEntity.noContent().build();
    }
}