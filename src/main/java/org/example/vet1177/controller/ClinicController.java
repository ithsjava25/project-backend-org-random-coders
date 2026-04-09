package org.example.vet1177.controller;

import jakarta.validation.Valid;
import org.example.vet1177.dto.request.clinic.CreateClinicRequest;
import org.example.vet1177.dto.request.clinic.UpdateClinicRequest;
import org.example.vet1177.dto.response.clinic.ClinicResponse;
import org.example.vet1177.services.ClinicService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/clinics")
public class ClinicController {

    private static final Logger log = LoggerFactory.getLogger(ClinicController.class);

    private final ClinicService clinicService;

    public ClinicController(ClinicService clinicService) {
        this.clinicService = clinicService;
    }

    // CREATE
    @PostMapping
    public ResponseEntity<ClinicResponse> create(
            @Valid @RequestBody CreateClinicRequest request) {

        log.info("POST /api/clinics - creating clinic");
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
        log.info("GET /api/clinics");
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
        log.info("GET /api/clinics/{}", id);
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

        log.info("PUT /api/clinics/{}", id);
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
        log.info("DELETE /api/clinics/{}", id);
        clinicService.delete(id);
        return ResponseEntity.noContent().build();
    }
}