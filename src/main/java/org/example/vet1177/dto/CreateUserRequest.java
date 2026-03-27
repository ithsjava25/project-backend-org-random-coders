package org.example.vet1177.dto;

import org.example.vet1177.entities.Role;

public class CreateUserRequest {

    private String name;
    private String email;
    private String password;
    private Role role;

    public String getName(){
        return name;
    }

    public String getEmail(){
        return email;
    }

}
