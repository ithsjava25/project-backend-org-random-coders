package org.example.vet1177.controller;

import jakarta.validation.Valid;
import org.example.vet1177.entities.*;
import org.example.vet1177.policy.MedicalRecordPolicy;
import org.example.vet1177.services.MedicalRecordService;
import org.example.vet1177.services.UserService;
import org.springframework.http.ResponseEntity;
import org.example.vet1177.dto.request.medicalrecord.*;
import org.example.vet1177.dto.response.medicalrecord.*;
import org.example.vet1177.exception.ForbiddenException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/medical-records")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;
    private final MedicalRecordPolicy medicalRecordPolicy;
    private final UserService userService;

    public MedicalRecordController(
            MedicalRecordService medicalRecordService,
            MedicalRecordPolicy medicalRecordPolicy,
            UserService userService) {
        this.medicalRecordService = medicalRecordService;
        this.medicalRecordPolicy = medicalRecordPolicy;
        this.userService = userService;
    }

    // POST /api/medical-records
    @PostMapping
    @Transactional
    public ResponseEntity<MedicalRecordResponse> create(
            @Valid @RequestBody CreateMedicalRecordRequest request,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                MedicalRecordResponse.from(
                        medicalRecordService.create(
                                request.title(),
                                request.description(),
                                request.petId(),
                                request.clinicId(),
                                currentUser
                        )
                )
        );
    }

    // GET /api/medical-records/{id}
    @GetMapping("/{id}")
    @Transactional(readOnly = true)
    public ResponseEntity<MedicalRecordResponse> getById(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {

        MedicalRecord record = medicalRecordService.getById(id);
        medicalRecordPolicy.canView(currentUser, record);
        return ResponseEntity.ok(MedicalRecordResponse.from(record));
    }

    // GET /api/medical-records/my-records (för OWNER)
    @GetMapping("/my-records")
    @Transactional(readOnly = true)
    public ResponseEntity<List<MedicalRecordSummaryResponse>> getMyRecords(
            @AuthenticationPrincipal User currentUser) {

        if (currentUser.getRole() != Role.OWNER) {
            throw new ForbiddenException("Endast djurägare kan se sina egna ärenden");
        }

        return ResponseEntity.ok(
                medicalRecordService.getByOwner(currentUser.getId())
                        .stream()
                        .map(MedicalRecordSummaryResponse::from)
                        .toList()
        );
    }


    @GetMapping("/owner/{ownerId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<MedicalRecordSummaryResponse>> getByOwner(
            @PathVariable UUID ownerId,
            @AuthenticationPrincipal User currentUser) {

        if (currentUser.getRole() == Role.OWNER &&
                !currentUser.getId().equals(ownerId)) {
            throw new ForbiddenException("Du kan bara se dina egna ärenden");
        }

        return ResponseEntity.ok(
                medicalRecordService.getByOwnerAllowedForUser(ownerId, currentUser)
                        .stream()
                        .map(MedicalRecordSummaryResponse::from)
                        .toList()
        );
    }

    // GET /api/medical-records/pet/{petId}
    // I controllern — enklare
    @GetMapping("/pet/{petId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<MedicalRecordSummaryResponse>> getByPet(
            @PathVariable UUID petId,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
                medicalRecordService.getByPetAllowedForUser(petId, currentUser)
                        .stream()
                        .map(MedicalRecordSummaryResponse::from)
                        .toList()
        );
    }

    // GET /api/medical-records/clinic/{clinicId}
    @GetMapping("/clinic/{clinicId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<MedicalRecordSummaryResponse>> getByClinic(
            @PathVariable UUID clinicId,
            @AuthenticationPrincipal User currentUser) {

        medicalRecordPolicy.canViewClinic(currentUser, clinicId);
        return ResponseEntity.ok(
                medicalRecordService.getByClinic(clinicId)
                        .stream()
                        .map(MedicalRecordSummaryResponse::from)
                        .toList()
        );
    }

    // GET /api/medical-records/clinic/{clinicId}/status/{status}
    @GetMapping("/clinic/{clinicId}/status/{status}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<MedicalRecordSummaryResponse>> getByClinicAndStatus(
            @PathVariable UUID clinicId,
            @PathVariable RecordStatus status,
            @AuthenticationPrincipal User currentUser) {

        medicalRecordPolicy.canViewClinic(currentUser, clinicId);
        return ResponseEntity.ok(
                medicalRecordService.getByClinicAndStatus(clinicId, status)
                        .stream()
                        .map(MedicalRecordSummaryResponse::from)
                        .toList()
        );
    }

    // PUT /api/medical-records/{id}
    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity<MedicalRecordResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateMedicalRecordRequest request,
            @AuthenticationPrincipal User currentUser) {

        MedicalRecord record = medicalRecordService.getById(id);
        medicalRecordPolicy.canUpdate(currentUser, record);

        return ResponseEntity.ok(
                MedicalRecordResponse.from(
                        medicalRecordService.update(
                                id,
                                request.title(),
                                request.description(),
                                currentUser
                        )
                )
        );
    }

    // PUT /api/medical-records/{id}/assign-vet
    @PutMapping("/{id}/assign-vet")
    @Transactional
    public ResponseEntity<MedicalRecordResponse> assignVet(
            @PathVariable UUID id,
            @Valid @RequestBody AssignVetRequest request,
            @AuthenticationPrincipal User currentUser) {

        MedicalRecord record = medicalRecordService.getById(id);
        User vetToAssign = userService.getById(request.vetId());
        medicalRecordPolicy.canAssignVet(currentUser, record, vetToAssign);

        return ResponseEntity.ok(
                MedicalRecordResponse.from(
                        medicalRecordService.assignVet(id, vetToAssign, currentUser)
                )
        );
    }

    // PUT /api/medical-records/{id}/status
    @PutMapping("/{id}/status")
    @Transactional
    public ResponseEntity<MedicalRecordResponse> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateStatusRequest request,
            @AuthenticationPrincipal User currentUser) {

        MedicalRecord record = medicalRecordService.getById(id);
        medicalRecordPolicy.canUpdateStatus(currentUser, record, request.status());

        return ResponseEntity.ok(
                MedicalRecordResponse.from(
                        medicalRecordService.updateStatus(
                                id,
                                request.status(),
                                currentUser
                        )
                )
        );
    }

    // PUT /api/medical-records/{id}/close
    @PutMapping("/{id}/close")
    @Transactional
    public ResponseEntity<MedicalRecordResponse> close(
            @PathVariable UUID id,
            @AuthenticationPrincipal User currentUser) {

        MedicalRecord record = medicalRecordService.getById(id);
        medicalRecordPolicy.canClose(currentUser, record);

        return ResponseEntity.ok(
                MedicalRecordResponse.from(
                        medicalRecordService.close(id, currentUser)
                )
        );
    }
}