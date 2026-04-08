package org.example.vet1177.services;

import org.example.vet1177.entities.*;
import org.example.vet1177.exception.BusinessRuleException;
import org.example.vet1177.exception.ResourceNotFoundException;
import org.example.vet1177.policy.MedicalRecordPolicy;
import org.example.vet1177.repository.ClinicRepository;
import org.example.vet1177.repository.MedicalRecordRepository;
import org.example.vet1177.repository.PetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MedicalRecordServiceTest {

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @Mock
    private PetRepository petRepository;

    @Mock
    private ClinicRepository clinicRepository;

    @Mock
    private MedicalRecordPolicy medicalRecordPolicy;

    @Mock
    private ActivityLogService activityLogService;

    @InjectMocks
    private MedicalRecordService medicalRecordService;

    private User currentUser;
    private User owner;
    private Pet pet;
    private Clinic clinic;
    private MedicalRecord record;
    private UUID petId;
    private UUID clinicId;
    private UUID recordId;
    private UUID ownerId;

    @BeforeEach
    void setUp() {
        petId = UUID.randomUUID();
        clinicId = UUID.randomUUID();
        recordId = UUID.randomUUID();
        ownerId = UUID.randomUUID();

        currentUser = new User("Dr. Sara Lindqvist", "sara@vet.se", "hash", Role.VET);
        owner = new User("Anna Ägare", "anna@example.se", "hash", Role.OWNER);

        clinic = new Clinic();
        pet = new Pet();
        pet.setOwner(owner);

        record = new MedicalRecord();
        record.setId(recordId);
        record.setTitle("Halsont");
        record.setDescription("Hostar mycket");
        record.setPet(pet);
        record.setOwner(owner);
        record.setClinic(clinic);
        record.setCreatedBy(currentUser);
        record.setStatus(RecordStatus.OPEN);
    }

    // -------------------------------------------------------------------------
    // create
    // -------------------------------------------------------------------------

    @Test
    void create_shouldSaveAndReturnRecord() {
        when(petRepository.findById(petId)).thenReturn(Optional.of(pet));
        when(clinicRepository.findById(clinicId)).thenReturn(Optional.of(clinic));
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(record);

        MedicalRecord result = medicalRecordService.create("Halsont", "Hostar mycket", petId, clinicId, currentUser);

        assertThat(result).isEqualTo(record);
        verify(medicalRecordRepository).save(any(MedicalRecord.class));
    }

    @Test
    void create_shouldCallPolicyCanCreate() {
        when(petRepository.findById(petId)).thenReturn(Optional.of(pet));
        when(clinicRepository.findById(clinicId)).thenReturn(Optional.of(clinic));
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(record);

        medicalRecordService.create("Halsont", "Hostar mycket", petId, clinicId, currentUser);

        verify(medicalRecordPolicy).canCreate(currentUser, pet, clinic);
    }

    @Test
    void create_shouldLogActivity() {
        when(petRepository.findById(petId)).thenReturn(Optional.of(pet));
        when(clinicRepository.findById(clinicId)).thenReturn(Optional.of(clinic));
        when(medicalRecordRepository.save(any(MedicalRecord.class))).thenReturn(record);

        medicalRecordService.create("Halsont", "Hostar mycket", petId, clinicId, currentUser);

        verify(activityLogService).log(ActivityType.CASE_CREATED, "Ärende skapat", currentUser, record);
    }

    @Test
    void create_whenPetNotFound_shouldThrowResourceNotFoundException() {
        when(petRepository.findById(petId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicalRecordService.create("Halsont", "x", petId, clinicId, currentUser))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(medicalRecordRepository, never()).save(any());
    }

    @Test
    void create_whenClinicNotFound_shouldThrowResourceNotFoundException() {
        when(petRepository.findById(petId)).thenReturn(Optional.of(pet));
        when(clinicRepository.findById(clinicId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicalRecordService.create("Halsont", "x", petId, clinicId, currentUser))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(medicalRecordRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // getById
    // -------------------------------------------------------------------------

    @Test
    void getById_shouldReturnRecord() {
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(record));

        MedicalRecord result = medicalRecordService.getById(recordId);

        assertThat(result).isEqualTo(record);
    }

    @Test
    void getById_whenNotFound_shouldThrowResourceNotFoundException() {
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicalRecordService.getById(recordId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // -------------------------------------------------------------------------
    // getByPet / getByOwner / getByClinic / getByClinicAndStatus
    // -------------------------------------------------------------------------

    @Test
    void getByPet_shouldReturnRecordsForPet() {
        when(medicalRecordRepository.findByPetId(petId)).thenReturn(List.of(record));

        List<MedicalRecord> result = medicalRecordService.getByPet(petId);

        assertThat(result).containsExactly(record);
    }

    @Test
    void getByOwner_shouldReturnRecordsForOwner() {
        when(medicalRecordRepository.findByOwnerId(ownerId)).thenReturn(List.of(record));

        List<MedicalRecord> result = medicalRecordService.getByOwner(ownerId);

        assertThat(result).containsExactly(record);
    }

    @Test
    void getByClinic_shouldReturnRecordsForClinic() {
        when(medicalRecordRepository.findByClinicId(clinicId)).thenReturn(List.of(record));

        List<MedicalRecord> result = medicalRecordService.getByClinic(clinicId);

        assertThat(result).containsExactly(record);
    }

    @Test
    void getByClinicAndStatus_shouldReturnFilteredRecords() {
        when(medicalRecordRepository.findByClinicIdAndStatus(clinicId, RecordStatus.OPEN))
                .thenReturn(List.of(record));

        List<MedicalRecord> result = medicalRecordService.getByClinicAndStatus(clinicId, RecordStatus.OPEN);

        assertThat(result).containsExactly(record);
    }

    // -------------------------------------------------------------------------
    // getByPetAllowedForUser / getByOwnerAllowedForUser
    // -------------------------------------------------------------------------

    @Test
    void getByPetAllowedForUser_shouldReturnOnlyAllowedRecords() {
        MedicalRecord other = new MedicalRecord();
        other.setId(UUID.randomUUID());
        when(medicalRecordRepository.findByPetId(petId)).thenReturn(List.of(record, other));
        when(medicalRecordPolicy.isAllowed(currentUser, record)).thenReturn(true);
        when(medicalRecordPolicy.isAllowed(currentUser, other)).thenReturn(false);

        List<MedicalRecord> result = medicalRecordService.getByPetAllowedForUser(petId, currentUser);

        assertThat(result).containsExactly(record);
    }

    @Test
    void getByOwnerAllowedForUser_shouldReturnOnlyAllowedRecords() {
        MedicalRecord other = new MedicalRecord();
        other.setId(UUID.randomUUID());
        when(medicalRecordRepository.findByOwnerId(ownerId)).thenReturn(List.of(record, other));
        when(medicalRecordPolicy.isAllowed(currentUser, record)).thenReturn(true);
        when(medicalRecordPolicy.isAllowed(currentUser, other)).thenReturn(false);

        List<MedicalRecord> result = medicalRecordService.getByOwnerAllowedForUser(ownerId, currentUser);

        assertThat(result).containsExactly(record);
    }

    // -------------------------------------------------------------------------
    // update
    // -------------------------------------------------------------------------

    @Test
    void update_shouldSaveAndReturnUpdatedRecord() {
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(medicalRecordRepository.save(record)).thenReturn(record);

        MedicalRecord result = medicalRecordService.update(recordId, "Ny titel", "Ny beskrivning", currentUser);

        assertThat(result.getTitle()).isEqualTo("Ny titel");
        assertThat(result.getDescription()).isEqualTo("Ny beskrivning");
        verify(medicalRecordRepository).save(record);
    }

    @Test
    void update_shouldLogActivity() {
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(medicalRecordRepository.save(record)).thenReturn(record);

        medicalRecordService.update(recordId, "Ny titel", "Ny beskrivning", currentUser);

        verify(activityLogService).log(ActivityType.UPDATED, "Ärende uppdaterat", currentUser, record);
    }

    @Test
    void update_whenRecordClosed_shouldThrowBusinessRuleException() {
        record.setStatus(RecordStatus.CLOSED);
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(record));

        assertThatThrownBy(() -> medicalRecordService.update(recordId, "x", "y", currentUser))
                .isInstanceOf(BusinessRuleException.class);

        verify(medicalRecordRepository, never()).save(any());
    }

    @Test
    void update_whenNotFound_shouldThrowResourceNotFoundException() {
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> medicalRecordService.update(recordId, "x", "y", currentUser))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(medicalRecordRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // assignVet
    // -------------------------------------------------------------------------

    @Test
    void assignVet_shouldAssignAndSetInProgress() {
        User vet = new User("Dr. Erik", "erik@vet.se", "hash", Role.VET);
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(medicalRecordRepository.save(record)).thenReturn(record);

        MedicalRecord result = medicalRecordService.assignVet(recordId, vet, currentUser);

        assertThat(result.getAssignedVet()).isEqualTo(vet);
        assertThat(result.getStatus()).isEqualTo(RecordStatus.IN_PROGRESS);
    }

    @Test
    void assignVet_shouldLogActivity() {
        User vet = new User("Dr. Erik", "erik@vet.se", "hash", Role.VET);
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(medicalRecordRepository.save(record)).thenReturn(record);

        medicalRecordService.assignVet(recordId, vet, currentUser);

        verify(activityLogService).log(ActivityType.ASSIGNED, "Veterinär tilldelad", currentUser, record);
    }

    @Test
    void assignVet_whenRecordClosed_shouldThrowBusinessRuleException() {
        User vet = new User("Dr. Erik", "erik@vet.se", "hash", Role.VET);
        record.setStatus(RecordStatus.CLOSED);
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(record));

        assertThatThrownBy(() -> medicalRecordService.assignVet(recordId, vet, currentUser))
                .isInstanceOf(BusinessRuleException.class);

        verify(medicalRecordRepository, never()).save(any());
    }

    @Test
    void assignVet_whenAssigneeNotVet_shouldThrowBusinessRuleException() {
        User notVet = new User("Anna", "anna@example.se", "hash", Role.OWNER);
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(record));

        assertThatThrownBy(() -> medicalRecordService.assignVet(recordId, notVet, currentUser))
                .isInstanceOf(BusinessRuleException.class);

        verify(medicalRecordRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // updateStatus
    // -------------------------------------------------------------------------

    @Test
    void updateStatus_shouldUpdateAndReturnRecord() {
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(medicalRecordRepository.save(record)).thenReturn(record);

        MedicalRecord result = medicalRecordService.updateStatus(recordId, RecordStatus.AWAITING_INFO, currentUser);

        assertThat(result.getStatus()).isEqualTo(RecordStatus.AWAITING_INFO);
    }

    @Test
    void updateStatus_whenSetToClosed_shouldSetClosedAt() {
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(medicalRecordRepository.save(record)).thenReturn(record);

        MedicalRecord result = medicalRecordService.updateStatus(recordId, RecordStatus.CLOSED, currentUser);

        assertThat(result.getClosedAt()).isNotNull();
    }

    @Test
    void updateStatus_shouldLogActivity() {
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(medicalRecordRepository.save(record)).thenReturn(record);

        medicalRecordService.updateStatus(recordId, RecordStatus.AWAITING_INFO, currentUser);

        verify(activityLogService).log(
                eq(ActivityType.STATUS_CHANGED),
                contains("AWAITING_INFO"),
                eq(currentUser),
                eq(record));
    }

    @Test
    void updateStatus_whenAlreadyClosed_shouldThrowBusinessRuleException() {
        record.setStatus(RecordStatus.CLOSED);
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(record));

        assertThatThrownBy(() -> medicalRecordService.updateStatus(recordId, RecordStatus.OPEN, currentUser))
                .isInstanceOf(BusinessRuleException.class);

        verify(medicalRecordRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // close
    // -------------------------------------------------------------------------

    @Test
    void close_shouldSetStatusClosedAndClosedAt() {
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(medicalRecordRepository.save(record)).thenReturn(record);

        MedicalRecord result = medicalRecordService.close(recordId, currentUser);

        assertThat(result.getStatus()).isEqualTo(RecordStatus.CLOSED);
        assertThat(result.getClosedAt()).isNotNull();
    }

    @Test
    void close_shouldLogActivity() {
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(record));
        when(medicalRecordRepository.save(record)).thenReturn(record);

        medicalRecordService.close(recordId, currentUser);

        verify(activityLogService).log(ActivityType.STATUS_CHANGED, "Ärende stängt", currentUser, record);
    }

    @Test
    void close_whenAlreadyClosed_shouldThrowBusinessRuleException() {
        record.setStatus(RecordStatus.CLOSED);
        when(medicalRecordRepository.findById(recordId)).thenReturn(Optional.of(record));

        assertThatThrownBy(() -> medicalRecordService.close(recordId, currentUser))
                .isInstanceOf(BusinessRuleException.class);

        verify(medicalRecordRepository, never()).save(any());
    }

    // -------------------------------------------------------------------------
    // save
    // -------------------------------------------------------------------------

    @Test
    void save_shouldDelegateToRepository() {
        when(medicalRecordRepository.save(record)).thenReturn(record);

        MedicalRecord result = medicalRecordService.save(record);

        assertThat(result).isEqualTo(record);
        verify(medicalRecordRepository).save(record);
    }
}
