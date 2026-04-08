package org.example.vet1177.policy;

import org.example.vet1177.entities.Pet;
import org.example.vet1177.entities.Role;
import org.example.vet1177.entities.User;
import org.junit.jupiter.api.BeforeEach;

import java.lang.reflect.Field;
import java.util.UUID;

class PetPolicyTest {

    private PetPolicy policy;

    private User admin;
    private User owner;
    private User otherOwner;
    private User vet;

    private Pet pet;

    @BeforeEach
    void setUp() throws Exception {
        policy = new PetPolicy();

        admin = new User("Admin Adminsson", "admin@vet.se", "hash", Role.ADMIN);
        setPrivateField(admin, "id", UUID.randomUUID());

        owner = new User("Anna Ägare", "anna@mail.se", "hash", Role.OWNER);
        setPrivateField(owner, "id", UUID.randomUUID());

        otherOwner = new User("Karin Annan", "karin@mail.se", "hash", Role.OWNER);
        setPrivateField(otherOwner, "id", UUID.randomUUID());

        vet = new User("Dr. Erik Vet", "erik@vet.se", "hash", Role.VET);
        setPrivateField(vet, "id", UUID.randomUUID());

        pet = new Pet();
        pet.setOwner(owner);
    }

    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

}
