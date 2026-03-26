package org.example.vet1177.services;

import org.example.vet1177.entities.Role;
import org.example.vet1177.entities.User;
import org.example.vet1177.repository.UserRepository;
import org.springframework.stereotype.Service;

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

    public User getById(UUID id){
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public User getByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

}
