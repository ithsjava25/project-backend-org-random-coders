package org.example.vet1177.services;

import org.example.vet1177.dto.request.user.UserRequest;
import org.example.vet1177.dto.request.user.UserUpdateRequest;
import org.example.vet1177.dto.response.user.UserResponse;
import org.example.vet1177.entities.Clinic;
import org.example.vet1177.entities.Role;
import org.example.vet1177.entities.User;
import org.example.vet1177.exception.BusinessRuleException;
import org.example.vet1177.exception.ResourceNotFoundException;
import org.example.vet1177.repository.ClinicRepository;
import org.example.vet1177.repository.MedicalRecordRepository;
import org.example.vet1177.repository.PetRepository;
import org.example.vet1177.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClinicRepository clinicRepository;

    @Mock
    private PetRepository petRepository;

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @InjectMocks
    private UserService userService;

    private UUID userId;
    private UUID clinicId;
    private User ownerUser;
    private User vetUser;
    private User adminUser;
    private Clinic clinic;

    @BeforeEach
    void setUp() throws Exception {
        userId = UUID.randomUUID();
        clinicId = UUID.randomUUID();

        clinic = new Clinic("Djurkliniken", "Storgatan 1", "+4670123456");
        setPrivateField(clinic, "id", clinicId);

        ownerUser = new User("Anna Ägare", "anna@example.se", "hashedPw", Role.OWNER);
        setPrivateField(ownerUser, "id", userId);

        vetUser = new User("Dr. Vet", "vet@klinik.se", "hashedPw", Role.VET, clinic);
        setPrivateField(vetUser, "id", UUID.randomUUID());

        adminUser = new User("Admin Adminsson", "admin@example.se", "hashedPw", Role.ADMIN);
        setPrivateField(adminUser, "id", UUID.randomUUID());
    }

    // createUser
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    void createUser_owner_withoutClinic_returnsResponse() {
        UserRequest request = ownerRequest("anna@example.se", null);

        when(userRepository.existsByEmail("anna@example.se")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(ownerUser);

        UserResponse response = userService.createUser(request);

        assertThat(response.getEmail()).isEqualTo("anna@example.se");
        assertThat(response.getRole()).isEqualTo(Role.OWNER);
        assertThat(response.getClinicId()).isNull();
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword())
                .as("lösenordet ska vara hashat innan persist")
                .isNotEqualTo(request.getPassword());
    }

    @Test
    void createUser_vet_withClinic_returnsResponse() {
        UserRequest request = vetRequest("vet@klinik.se", clinicId);

        when(userRepository.existsByEmail("vet@klinik.se")).thenReturn(false);
        when(clinicRepository.findById(clinicId)).thenReturn(Optional.of(clinic));
        when(userRepository.save(any(User.class))).thenReturn(vetUser);

        UserResponse response = userService.createUser(request);

        assertThat(response.getRole()).isEqualTo(Role.VET);
        assertThat(response.getClinicId()).isEqualTo(clinicId);
    }

    @Test
    void createUser_emailAlreadyExists_throwsBusinessRuleException() {
        UserRequest request = ownerRequest("anna@example.se", null);
        when(userRepository.existsByEmail("anna@example.se")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Email används redan");

        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_vet_withoutClinic_throwsBusinessRuleException() {
        UserRequest request = vetRequest("vet@klinik.se", null);
        when(userRepository.existsByEmail("vet@klinik.se")).thenReturn(false);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Veterinär måste vara kopplad till en klinik");
    }

    @Test
    void createUser_owner_withClinic_throwsBusinessRuleException() {
        UserRequest request = ownerRequest("anna@example.se", clinicId);
        when(userRepository.existsByEmail("anna@example.se")).thenReturn(false);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Endast veterinärer kan kopplas till en klinik");
    }

    @Test
    void createUser_vet_clinicNotFound_throwsResourceNotFoundException() {
        UUID unknownClinicId = UUID.randomUUID();
        UserRequest request = vetRequest("vet@klinik.se", unknownClinicId);

        when(userRepository.existsByEmail("vet@klinik.se")).thenReturn(false);
        when(clinicRepository.findById(unknownClinicId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void createUser_dataIntegrityViolation_throwsBusinessRuleException() {
        UserRequest request = ownerRequest("anna@example.se", null);

        when(userRepository.existsByEmail("anna@example.se")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenThrow(new DataIntegrityViolationException("dup"));

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Email används redan");
    }

    //getByEmail


    @Test
    void getByEmail_existingEmail_returnsUser() {
        when(userRepository.findByEmail("anna@example.se")).thenReturn(Optional.of(ownerUser));

        User result = userService.getByEmail("anna@example.se");

        assertThat(result).isEqualTo(ownerUser);
    }

    @Test
    void getByEmail_unknownEmail_throwsResourceNotFoundException() {
        when(userRepository.findByEmail("okänd@example.se")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getByEmail("okänd@example.se"))
                .isInstanceOf(ResourceNotFoundException.class);
    }


    // getUserEntityById

    @Test
    void getUserEntityById_existingId_returnsUserEntity() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(ownerUser));

        User result = userService.getUserEntityById(userId);

        assertThat(result).isEqualTo(ownerUser);
    }

    @Test
    void getUserEntityById_unknownId_throwsResourceNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(userRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserEntityById(unknownId))
                .isInstanceOf(ResourceNotFoundException.class);
    }


    // getById

    @Test
    void getById_existingId_returnsUserResponse() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(ownerUser));

        UserResponse response = userService.getById(userId);

        assertThat(response.getId()).isEqualTo(ownerUser.getId());
        assertThat(response.getEmail()).isEqualTo("anna@example.se");
        assertThat(response.getRole()).isEqualTo(Role.OWNER);
    }

    @Test
    void getById_unknownId_throwsResourceNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(userRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getById(unknownId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // getAllUsers

    @Test
    void getAllUsers_returnsAllMappedResponses() {
        when(userRepository.findAll()).thenReturn(List.of(ownerUser, vetUser, adminUser));

        List<UserResponse> responses = userService.getAllUsers();

        assertThat(responses).hasSize(3);
        assertThat(responses).extracting(UserResponse::getRole)
                .containsExactlyInAnyOrder(Role.OWNER, Role.VET, Role.ADMIN);
    }

    @Test
    void getAllUsers_emptyRepo_returnsEmptyList() {
        when(userRepository.findAll()).thenReturn(List.of());

        List<UserResponse> responses = userService.getAllUsers();

        assertThat(responses).isEmpty();
    }


    //updateUser

    @Test
    void updateUser_changeName_updatesAndReturns() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setName("Nytt Namn");

        when(userRepository.findById(userId)).thenReturn(Optional.of(ownerUser));
        when(userRepository.save(ownerUser)).thenReturn(ownerUser);

        UserResponse response = userService.updateUser(userId, request);

        assertThat(ownerUser.getName()).isEqualTo("Nytt Namn");
        verify(userRepository).save(ownerUser);
    }

    @Test
    void updateUser_changeEmail_uniqueEmail_updatesAndReturns() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("ny@example.se");

        when(userRepository.findById(userId)).thenReturn(Optional.of(ownerUser));
        when(userRepository.existsByEmailAndIdNot("ny@example.se", userId)).thenReturn(false);
        when(userRepository.save(ownerUser)).thenReturn(ownerUser);

        userService.updateUser(userId, request);

        assertThat(ownerUser.getEmail()).isEqualTo("ny@example.se");
    }

    @Test
    void updateUser_changeEmail_duplicateEmail_throwsBusinessRuleException() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("tagen@example.se");

        when(userRepository.findById(userId)).thenReturn(Optional.of(ownerUser));
        when(userRepository.existsByEmailAndIdNot("tagen@example.se", userId)).thenReturn(true);

        assertThatThrownBy(() -> userService.updateUser(userId, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Email används redan");

        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_vet_changeClinic_updatesClinic() {
        UUID newClinicId = UUID.randomUUID();
        Clinic newClinic = new Clinic("Ny Klinik", "Nya gatan 2", "+4670000000");

        UserUpdateRequest request = new UserUpdateRequest();
        request.setClinicId(newClinicId);

        when(userRepository.findById(vetUser.getId())).thenReturn(Optional.of(vetUser));
        when(clinicRepository.findById(newClinicId)).thenReturn(Optional.of(newClinic));
        when(userRepository.save(vetUser)).thenReturn(vetUser);

        userService.updateUser(vetUser.getId(), request);

        assertThat(vetUser.getClinic()).isEqualTo(newClinic);
    }

    @Test
    void updateUser_owner_setClinic_throwsBusinessRuleException() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setClinicId(clinicId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(ownerUser));

        assertThatThrownBy(() -> userService.updateUser(userId, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Endast veterinärer kan kopplas till en klinik");
    }

    @Test
    void updateUser_unknownId_throwsResourceNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(userRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.updateUser(unknownId, new UserUpdateRequest()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateUser_dataIntegrityViolation_throwsBusinessRuleException() {
        UserUpdateRequest request = new UserUpdateRequest();
        request.setEmail("ny@example.se");

        when(userRepository.findById(userId)).thenReturn(Optional.of(ownerUser));
        when(userRepository.existsByEmailAndIdNot("ny@example.se", userId)).thenReturn(false);
        when(userRepository.save(any())).thenThrow(new DataIntegrityViolationException("dup"));

        assertThatThrownBy(() -> userService.updateUser(userId, request))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("Email används redan");
    }

    //DeleteUser

    @Test
    void deleteUser_noConstraints_deletesSuccessfully() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(ownerUser));
        when(petRepository.existsByOwner_Id(userId)).thenReturn(false);
        when(medicalRecordRepository.existsByOwnerId(userId)).thenReturn(false);
        when(medicalRecordRepository.existsByAssignedVetId(userId)).thenReturn(false);
        when(medicalRecordRepository.existsByCreatedById(userId)).thenReturn(false);
        when(medicalRecordRepository.existsByUpdatedById(userId)).thenReturn(false);

        userService.deleteUser(userId);

        verify(userRepository).delete(ownerUser);
    }

    @Test
    void deleteUser_unknownId_throwsResourceNotFoundException() {
        UUID unknownId = UUID.randomUUID();
        when(userRepository.findById(unknownId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.deleteUser(unknownId))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUser_userHasPets_throwsBusinessRuleException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(ownerUser));
        when(petRepository.existsByOwner_Id(userId)).thenReturn(true);

        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("kopplade djur");

        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUser_userIsRecordOwner_throwsBusinessRuleException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(ownerUser));
        when(petRepository.existsByOwner_Id(userId)).thenReturn(false);
        when(medicalRecordRepository.existsByOwnerId(userId)).thenReturn(true);

        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("ägare på journalposter");

        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUser_userIsAssignedVet_throwsBusinessRuleException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(ownerUser));
        when(petRepository.existsByOwner_Id(userId)).thenReturn(false);
        when(medicalRecordRepository.existsByOwnerId(userId)).thenReturn(false);
        when(medicalRecordRepository.existsByAssignedVetId(userId)).thenReturn(true);

        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("tilldelad veterinär");

        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUser_userCreatedRecords_throwsBusinessRuleException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(ownerUser));
        when(petRepository.existsByOwner_Id(userId)).thenReturn(false);
        when(medicalRecordRepository.existsByOwnerId(userId)).thenReturn(false);
        when(medicalRecordRepository.existsByAssignedVetId(userId)).thenReturn(false);
        when(medicalRecordRepository.existsByCreatedById(userId)).thenReturn(true);

        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("skapat journalposter");

        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUser_userUpdatedRecords_throwsBusinessRuleException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(ownerUser));
        when(petRepository.existsByOwner_Id(userId)).thenReturn(false);
        when(medicalRecordRepository.existsByOwnerId(userId)).thenReturn(false);
        when(medicalRecordRepository.existsByAssignedVetId(userId)).thenReturn(false);
        when(medicalRecordRepository.existsByCreatedById(userId)).thenReturn(false);
        when(medicalRecordRepository.existsByUpdatedById(userId)).thenReturn(true);

        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("uppdaterat journalposter");

        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteUser_dataIntegrityViolation_throwsBusinessRuleException() {
        when(userRepository.findById(userId)).thenReturn(Optional.of(ownerUser));
        when(petRepository.existsByOwner_Id(userId)).thenReturn(false);
        when(medicalRecordRepository.existsByOwnerId(userId)).thenReturn(false);
        when(medicalRecordRepository.existsByAssignedVetId(userId)).thenReturn(false);
        when(medicalRecordRepository.existsByCreatedById(userId)).thenReturn(false);
        when(medicalRecordRepository.existsByUpdatedById(userId)).thenReturn(false);
        doThrow(new DataIntegrityViolationException("fk")).when(userRepository).delete(ownerUser);

        assertThatThrownBy(() -> userService.deleteUser(userId))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessageContaining("kopplade poster");
    }

    //Helper
    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private UserRequest vetRequest(String email, UUID clinicId) {
        UserRequest req = new UserRequest();
        req.setName("Dr. Vet");
        req.setEmail(email);
        req.setPassword("veterinär123");
        req.setRole(Role.VET);
        req.setClinicId(clinicId);
        return req;
    }

    private UserRequest ownerRequest(String email, UUID clinicId) {
        UserRequest req = new UserRequest();
        req.setName("Anna Ägare");
        req.setEmail(email);
        req.setPassword("lösenord123");
        req.setRole(Role.OWNER);
        req.setClinicId(clinicId);
        return req;
    }

}
