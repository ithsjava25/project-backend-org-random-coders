package org.example.vet1177.services;

import org.example.vet1177.dto.request.user.UserRequest;
import org.example.vet1177.dto.request.user.UserUpdateRequest;
import org.example.vet1177.dto.response.user.UserResponse;
import org.example.vet1177.entities.Clinic;
import org.example.vet1177.entities.Role;
import org.example.vet1177.entities.User;
import org.example.vet1177.exception.ResourceNotFoundException;
import org.example.vet1177.repository.ClinicRepository;
import org.example.vet1177.repository.UserRepository;
import org.springframework.stereotype.Service;



import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ClinicRepository clinicRepository;

    public UserService(UserRepository userRepository,
                       ClinicRepository clinicRepository){
        this.userRepository = userRepository;
        this.clinicRepository = clinicRepository;
    }
    // TODO: Hasha lösenordet med BCrypt när Spring Security implementeras
    public UserResponse createUser(UserRequest request) {
        validateEmailUnique(request.getEmail());
        User user = new User(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
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

        applyClinicRules(user, request.getClinicId());

        return mapToResponse(userRepository.save(user));
    }

    //Delete user
    public void deleteUser(UUID id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
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
            throw new IllegalArgumentException("Email används redan");
        }
    }

    private void validateEmailUniqueForUpdate(String email, UUID userId) {
        if (userRepository.existsByEmailAndIdNot(email, userId)) {
            throw new IllegalArgumentException("Email används redan");
        }
    }

    private void applyClinicRules(User user, UUID clinicId) {
        if (user.getRole() == Role.VET) {
            if (clinicId == null) {
                throw new IllegalArgumentException("Veterinär måste vara kopplad till en klinik");
            }

            Clinic clinic = clinicRepository.findById(clinicId)
                    .orElseThrow(() -> new ResourceNotFoundException("Clinic", clinicId));

            user.setClinic(clinic);
        } else {
            user.setClinic(null);
        }
    }
}
