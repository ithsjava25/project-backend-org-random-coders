package org.example.vet1177.policy;

import org.example.vet1177.entities.Pet;
import org.example.vet1177.entities.Role;
import org.example.vet1177.entities.User;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class PetPolicy {

    public boolean canCreate(User user) {
        return user.getRole() == Role.OWNER;
    }

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

    public boolean canViewOwnerPets(User user, UUID ownerId) {
        if (user.getRole() == Role.ADMIN) {
            return true;
        }

        return user.getRole() == Role.OWNER &&
                user.getId().equals(ownerId);
    }

    public boolean canUpdate(User user, Pet pet) {
        if (user.getRole() == Role.ADMIN) {
            return true;
        }

        return user.getRole() == Role.OWNER &&
                pet.getOwner() != null &&
                pet.getOwner().getId().equals(user.getId());
    }

    public boolean canDelete(User user, Pet pet) {
        if (user.getRole() == Role.ADMIN) {
            return true;
        }

        return user.getRole() == Role.OWNER &&
                pet.getOwner() != null &&
                pet.getOwner().getId().equals(user.getId());
    }
}