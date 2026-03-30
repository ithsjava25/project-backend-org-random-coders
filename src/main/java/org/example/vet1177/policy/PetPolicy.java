package org.example.vet1177.policy;

import org.example.vet1177.entities.Pet;
import org.example.vet1177.entities.Role;
import org.example.vet1177.entities.User;
import org.springframework.stereotype.Component;

@Component
public class PetPolicy {

    // Bara Owner kan skapa djur
    public boolean canCreate(User user) {
        return user.getRole() == Role.OWNER;
    }

    // Bara Admin och Owner kan se djur där de finns behörighet
    public boolean canView(User user, Pet pet) {
        if (user.getRole() == Role.ADMIN) {
            return true;
        }

        if (user.getRole() == Role.OWNER) {
            return pet.getOwner() != null &&
                    pet.getOwner().getId().equals(user.getId());
        }
        return false;
    }


}
