package org.example.vet1177.controller;

import org.example.vet1177.entities.User;
import org.example.vet1177.services.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    //skapa user
    @PostMapping
    public User createUser(@RequestBody CreateUserRequest request){
        return userService.createUser(
                request.getName(),
                request.getEmail(),
                request.getPassword(),
                request.getRoel()
        );
    }

    //Hämta user via ID
    @GetMapping("/{id}")
    public User getUserById(@PathVariable UUID id){
        return userService.getById(id);
    }

    //Hämta user via email
    @GetMapping("/email")
    public User getUserByEmail(@RequestParam String email){
        return userService.getByEmail(email);
    }

}
