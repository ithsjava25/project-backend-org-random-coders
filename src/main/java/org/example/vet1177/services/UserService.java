package org.example.vet1177.services;

import org.example.vet1177.dto.request.user.UserRequest;
import org.example.vet1177.dto.response.user.UserResponse;
import org.example.vet1177.entities.Clinic;
import org.example.vet1177.entities.Role;
import org.example.vet1177.entities.User;
import org.example.vet1177.exception.ResourceNotFoundException;
import org.example.vet1177.repository.UserRepository;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ClinicService clinicService;

    public UserService(UserRepository userRepository, ClinicService clinicService){
        this.userRepository = userRepository;
        this.clinicService = clinicService;
    }
    // TODO: Hasha lösenordet med BCrypt när Spring Security implementeras
    public UserResponse createUser(UserRequest request) {
        validateEmailUnique(request.getEmail());
        User user = new User(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getRole());
        applyClinicRules(user, request);
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
    public UserResponse updateUser(UUID id, UserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        validateEmailUniqueForUpdate(request.getEmail(), id);
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        applyClinicRules(user, request);
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

    private void applyClinicRules(User user, UserRequest request) {
        if (request.getRole() == Role.VET) {
            if (request.getClinicId() == null) {
                throw new IllegalArgumentException("Veterinär måste vara kopplad till en klinik");
            }

            Clinic clinic = clinicService.getById(request.getClinicId());
            user.setClinic(clinic);
        } else {
            user.setClinic(null);
        }
    }
}
