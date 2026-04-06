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
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;



import java.util.List;
import java.util.UUID;

@Service
public class UserService {
    // TODO: Implementera Spring Security för autentisering och auktorisering
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final UserRepository userRepository;
    private final ClinicRepository clinicRepository;
    private final PetRepository petRepository;
    private final MedicalRecordRepository medicalRecordRepository;

    public UserService(UserRepository userRepository,
                       ClinicRepository clinicRepository,
                       PetRepository petRepository,
                       MedicalRecordRepository medicalRecordRepository) {
        this.userRepository = userRepository;
        this.clinicRepository = clinicRepository;
        this.petRepository = petRepository;
        this.medicalRecordRepository = medicalRecordRepository;
    }

    public UserResponse createUser(UserRequest request) {
        validateEmailUnique(request.getEmail());
        String passwordHash = passwordEncoder.encode(request.getPassword());
        User user = new User(
                request.getName(),
                request.getEmail(),
                passwordHash,
                request.getRole());
        applyClinicRules(user, request.getClinicId());
        return mapToResponse(userRepository.save(user));
    }

    // TODO: GET /users/search?email= - Sök användare på email, kräver ADMIN-roll (implementera när Spring Security är på plats)
    public User getByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User", email));
    }

    // Returnerar User-entiteten, används internt när andra services behöver ett User-objekt. OK? - annars
    public User getUserEntityById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    // Returnerar UserResponse DTO, används av UserController för att exponera användardata till klienten.
    // Ändra anrop från getById() till getUserEntityById() i ActivityLogController
    public UserResponse getById(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return mapToResponse(user);
    }

    //Get all users
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    //Update user
    public UserResponse updateUser(UUID id, UserUpdateRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (request.getEmail() != null) {
            validateEmailUniqueForUpdate(request.getEmail(), id);
            user.setEmail(request.getEmail());
        }

        if (request.getName() != null) {
            user.setName(request.getName());
        }

        if (request.getClinicId() != null) {
            applyClinicRulesForUpdate(user, request.getClinicId());
        }

        return mapToResponse(userRepository.save(user));
    }

    //Delete user
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));

        if (petRepository.existsByOwner_Id(id)) {
            throw new BusinessRuleException("Användaren har kopplade djur och kan inte raderas");
        }
        if (petRepository.existsByOwner_Id(id)) {
            throw new BusinessRuleException("Användaren har kopplade djur och kan inte raderas");
        }
        if (medicalRecordRepository.existsByOwnerId(id)) {
            throw new BusinessRuleException("Användaren är ägare på journalposter och kan inte raderas");
        }
        if (medicalRecordRepository.existsByAssignedVetId(id)) {
            throw new BusinessRuleException("Användaren är tilldelad veterinär på journalposter och kan inte raderas");
        }
        if (medicalRecordRepository.existsByCreatedById(id)) {
            throw new BusinessRuleException("Användaren har skapat journalposter och kan inte raderas");
        }
        if (medicalRecordRepository.existsByUpdatedById(id)) {
            throw new BusinessRuleException("Användaren har uppdaterat journalposter och kan inte raderas");
        }

        userRepository.delete(user);
    }

    //Helper
    private UserResponse mapToResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.getClinic() != null ? user.getClinic().getId() : null,
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    private void validateEmailUnique(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new BusinessRuleException("Email används redan");
        }
    }

    private void validateEmailUniqueForUpdate(String email, UUID userId) {
        if (userRepository.existsByEmailAndIdNot(email, userId)) {
            throw new BusinessRuleException("Email används redan");
        }
    }

    // Vid skapande — VET måste ha klinik, övriga får inte ha det
    private void applyClinicRules(User user, UUID clinicId) {
        if (user.getRole() == Role.VET) {
            if (clinicId == null) {
                throw new BusinessRuleException("Veterinär måste vara kopplad till en klinik");
            }
            Clinic clinic = clinicRepository.findById(clinicId)
                    .orElseThrow(() -> new ResourceNotFoundException("Clinic", clinicId));
            user.setClinic(clinic);
        } else {
            user.setClinic(null);
        }
    }

    // Vid uppdatering — klinik ändras bara om clinicId skickas med
    private void applyClinicRulesForUpdate(User user, UUID clinicId) {
        if (user.getRole() != Role.VET) {
            throw new BusinessRuleException("Endast veterinärer kan kopplas till en klinik");
        }
        Clinic clinic = clinicRepository.findById(clinicId)
                .orElseThrow(() -> new ResourceNotFoundException("Clinic", clinicId));
        user.setClinic(clinic);
    }



}
