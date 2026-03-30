package org.example.vet1177.policy;

import org.example.vet1177.entities.Role;
import org.example.vet1177.entities.User;
import org.springframework.stereotype.Component;

@Component
public class PetPolicy {

    public boolean canCreate(User user) {
        return user.getRole() == Role.OWNER;
    }
}
