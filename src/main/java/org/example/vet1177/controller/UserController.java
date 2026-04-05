package org.example.vet1177.controller;

import org.example.vet1177.dto.response.user.UserResponse;
import org.example.vet1177.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    //GET /users- Hämta alla användare
    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // GEt /users/{id}- Hämta en användte

    //POST /users - skapa ny användare
//    @GetMapping
//    public UserResponse createUser() {
//        return ;
//    }

    //PUT /users/{id} - uppdatera användare

    //DELETE /users/{id} - Tar bort användare

}
