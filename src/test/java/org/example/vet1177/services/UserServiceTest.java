package org.example.vet1177.services;

import org.example.vet1177.dto.request.user.UserRequest;
import org.example.vet1177.dto.response.user.UserResponse;
import org.example.vet1177.entities.Clinic;
import org.example.vet1177.entities.Role;
import org.example.vet1177.entities.User;
import org.example.vet1177.exception.BusinessRuleException;
import org.example.vet1177.exception.ResourceNotFoundException;
import org.example.vet1177.repository.ClinicRepository;
import org.example.vet1177.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.lang.reflect.Field;
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
    private PetService petService;

    @Mock
    private MedicalRecordService medicalRecordService;

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
        verify(userRepository).save(any(User.class));
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
