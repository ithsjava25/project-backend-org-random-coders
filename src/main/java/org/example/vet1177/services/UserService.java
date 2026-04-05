package org.example.vet1177.services;

import org.example.vet1177.dto.response.user.UserResponse;
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

    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    public User createUser(String name, String email, String passwordHash, Role role){
        User user = new User(name, email, passwordHash, role);
        return userRepository.save(user);
    }

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

}
