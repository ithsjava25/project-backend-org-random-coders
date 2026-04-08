package org.example.vet1177.services;

import org.example.vet1177.dto.request.pet.PetRequest;
import org.example.vet1177.entities.*;
import org.example.vet1177.exception.BusinessRuleException;
import org.example.vet1177.exception.ForbiddenException;
import org.example.vet1177.exception.ResourceNotFoundException;
import org.example.vet1177.policy.PetPolicy;
import org.example.vet1177.repository.MedicalRecordRepository;
import org.example.vet1177.repository.PetRepository;
import org.example.vet1177.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PetServiceTest {

    @Mock(answer = org.mockito.Answers.RETURNS_DEEP_STUBS)
    private PetRepository petRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PetPolicy petPolicy;

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @InjectMocks
    private PetService petService;

    private User admin;
    private User owner;
    private User otherOwner;
    private User vet;
    private Clinic clinic;
    private Pet pet;
    private PetRequest petRequest;
    private UUID adminId;
    private UUID ownerId;
    private UUID otherOwnerId;
    private UUID vetId;
    private UUID petId;

    @BeforeEach
    void setUp( ) throws Exception{
        adminId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        otherOwnerId = UUID.randomUUID();
        vetId = UUID.randomUUID();
        petId = UUID.randomUUID();

        admin = new User("Admin Adminsson", "Admin@example.se", "lösenord123", Role.ADMIN);
        setPrivateField(admin, "id", adminId);
        owner = new User("Ägare Ägarsson", "Ägerettdjur@example.se", "ägarlösen123", Role.OWNER);
        setPrivateField(owner,"id", ownerId);
        otherOwner = new User("Annan Ägarsson", "annanägare@example.se", "lös123", Role.OWNER);
        setPrivateField(otherOwner,"id", otherOwnerId);
        clinic = new Clinic("Huvudkliniken", "storgatan 1", "+465872159125");
        setPrivateField(clinic,"id", UUID.randomUUID());
        vet = new User("Dr. Erik Vet", "erik@vet.se", "hash", Role.VET);
        setPrivateField(vet, "id", vetId);
        setPrivateField(vet, "clinic", clinic);

        pet = new Pet(owner, "Molly", "Hund", "Labrador", LocalDate.of(2020, 1, 1), new BigDecimal("12.50"));
        setPrivateField(pet, "id", petId);

        petRequest = new PetRequest();
        petRequest.setName("Harry");
        petRequest.setSpecies("Hund");
        petRequest.setBreed("labrador");
        petRequest.setDateOfBirth(LocalDate.of(2020,1, 1));
        petRequest.setWeightKg(new BigDecimal("12.50"));

    }
    // createPet
    @Test
    void createPet_ownerCreatingForThemself_shouldSaveAndReturnPet() {
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(petPolicy.canCreate(owner)).thenReturn(true);
        when(petRepository.save(any(Pet.class))).thenReturn(pet);

        Pet result = petService.createPet(ownerId, null, petRequest);

        assertThat(result).isEqualTo(pet);
        verify(petRepository).save(any(Pet.class));
    }

    @Test
    void createPet_ownerCreatingForThemself_shouldSetOwnerToCurrentUser() {
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(petPolicy.canCreate(owner)).thenReturn(true);
        when(petRepository.save(any(Pet.class))).thenAnswer(inv -> inv.getArgument(0));

        Pet result = petService.createPet(ownerId, null, petRequest);

        assertThat(result.getOwner()).isEqualTo(owner);
    }

    @Test
    void createPet_adminCreatingForOwner_shouldSetCorrectOwner() {
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(petPolicy.canCreate(admin)).thenReturn(true);
        when(petRepository.save(any(Pet.class))).thenAnswer(inv -> inv.getArgument(0));

        Pet result = petService.createPet(adminId, ownerId, petRequest);

        assertThat(result.getOwner()).isEqualTo(owner);
    }

    @Test
    void createPet_adminWithoutOwnerId_shouldThrowBusinessRuleException() {
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(petPolicy.canCreate(admin)).thenReturn(true);

        assertThatThrownBy(() -> petService.createPet(adminId, null, petRequest))
                .isInstanceOf(BusinessRuleException.class);

        verify(petRepository, never()).save(any());
    }

    @Test
    void createPet_adminWithOwnerIdThatIsNotOwnerRole_shouldThrowBusinessRuleException() {
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(userRepository.findById(vetId)).thenReturn(Optional.of(vet));
        when(petPolicy.canCreate(admin)).thenReturn(true);

        assertThatThrownBy(() -> petService.createPet(adminId, vetId, petRequest))
                .isInstanceOf(BusinessRuleException.class);

        verify(petRepository, never()).save(any());
    }

    @Test
    void createPet_userWithoutPermission_shouldThrowForbiddenException() {
        when(userRepository.findById(vetId)).thenReturn(Optional.of(vet));
        when(petPolicy.canCreate(vet)).thenReturn(false);

        assertThatThrownBy(() -> petService.createPet(vetId, null, petRequest))
                .isInstanceOf(ForbiddenException.class);

        verify(petRepository, never()).save(any());
    }

    @Test
    void createPet_userNotFound_shouldThrowResourceNotFoundException() {
        when(userRepository.findById(ownerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> petService.createPet(ownerId, null, petRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    //GetPetByID

    @Test
    void getPetById_adminCanViewAnyPet_shouldReturnPet() {
        when(petRepository.findById(petId)).thenReturn(Optional.of(pet));
        when(petPolicy.canView(admin, pet)).thenReturn(true);

        Pet result = petService.getPetById(petId, admin);

        assertThat(result).isEqualTo(pet);
    }

    @Test
    void getPetById_ownerCanViewOwnPet_shouldReturnPet() {
        when(petRepository.findById(petId)).thenReturn(Optional.of(pet));
        when(petPolicy.canView(owner, pet)).thenReturn(true);

        Pet result = petService.getPetById(petId, owner);

        assertThat(result).isEqualTo(pet);
    }

    @Test
    void getPetById_vetWithClinicAndJournalAccess_shouldReturnPet() {
        when(petRepository.findById(petId)).thenReturn(Optional.of(pet));
        when(petPolicy.canView(vet, pet)).thenReturn(false);
        when(medicalRecordRepository.existsByPetIdAndClinicId(petId, clinic.getId())).thenReturn(true);

        Pet result = petService.getPetById(petId, vet);

        assertThat(result).isEqualTo(pet);
    }

    @Test
    void getPetById_vetWithoutClinic_shouldThrowForbiddenException() throws Exception {
        User vetNoClinic = new User("Dr. Ingen", "ingen@vet.se", "hash", Role.VET);
        setPrivateField(vetNoClinic, "id", UUID.randomUUID());

        when(petRepository.findById(petId)).thenReturn(Optional.of(pet));
        when(petPolicy.canView(vetNoClinic, pet)).thenReturn(false);

        assertThatThrownBy(() -> petService.getPetById(petId, vetNoClinic))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getPetById_vetWithNoJournalAccess_shouldThrowForbiddenException() {
        when(petRepository.findById(petId)).thenReturn(Optional.of(pet));
        when(petPolicy.canView(vet, pet)).thenReturn(false);
        assertThatThrownBy(() -> petService.getPetById(petId, vet))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getPetById_ownerViewingOthersPet_shouldThrowForbiddenException() {
        when(petRepository.findById(petId)).thenReturn(Optional.of(pet));
        when(petPolicy.canView(otherOwner, pet)).thenReturn(false);

        assertThatThrownBy(() -> petService.getPetById(petId, otherOwner))
                .isInstanceOf(ForbiddenException.class);
    }

    @Test
    void getPetById_petNotFound_shouldThrowResourceNotFoundException() {
        when(petRepository.findById(petId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> petService.getPetById(petId, owner))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    //GetPetByOwner

    @Test
    void getPetsByOwner_ownerViewingOwnPets_shouldReturnList() {
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(petPolicy.canViewOwnerPets(owner, ownerId)).thenReturn(true);
        when(petRepository.findByOwnerId(ownerId)).thenReturn(List.of(pet));

        List<Pet> result = petService.getPetsByOwner(ownerId, ownerId);

        assertThat(result).containsExactly(pet);
    }

    @Test
    void getPetsByOwner_ownerViewingOthersPets_shouldThrowForbiddenException() {
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(petPolicy.canViewOwnerPets(owner, otherOwnerId)).thenReturn(false);

        assertThatThrownBy(() -> petService.getPetsByOwner(ownerId, otherOwnerId))
                .isInstanceOf(ForbiddenException.class);

        verify(petRepository, never()).findByOwnerId(any());
    }

    @Test
    void getPetsByOwner_admin_shouldReturnList() {
        when(userRepository.findById(adminId)).thenReturn(Optional.of(admin));
        when(petPolicy.canViewOwnerPets(admin, ownerId)).thenReturn(true);
        when(petRepository.findByOwnerId(ownerId)).thenReturn(List.of(pet));

        List<Pet> result = petService.getPetsByOwner(adminId, ownerId);

        assertThat(result).containsExactly(pet);
    }

    // UpdatePet

    @Test
    void updatePet_ownerUpdatingOwnPet_shouldSaveAndReturnUpdatedPet() {
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(petRepository.findById(petId)).thenReturn(Optional.of(pet));
        when(petPolicy.canUpdate(owner, pet)).thenReturn(true);
        when(petRepository.save(pet)).thenReturn(pet);

        Pet result = petService.updatePet(ownerId, petId, petRequest);

        assertThat(result.getName()).isEqualTo(petRequest.getName());
        verify(petRepository).save(pet);
    }

    @Test
    void updatePet_ownerUpdatingOthersPet_shouldThrowForbiddenException() {
        when(userRepository.findById(otherOwnerId)).thenReturn(Optional.of(otherOwner));
        when(petRepository.findById(petId)).thenReturn(Optional.of(pet));
        when(petPolicy.canUpdate(otherOwner, pet)).thenReturn(false);

        assertThatThrownBy(() -> petService.updatePet(otherOwnerId, petId, petRequest))
                .isInstanceOf(ForbiddenException.class);

        verify(petRepository, never()).save(any());
    }

    @Test
    void updatePet_petNotFound_shouldThrowResourceNotFoundException() {
        when(userRepository.findById(ownerId)).thenReturn(Optional.of(owner));
        when(petRepository.findById(petId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> petService.updatePet(ownerId, petId, petRequest))
                .isInstanceOf(ResourceNotFoundException.class);
    }
    //helper
    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
