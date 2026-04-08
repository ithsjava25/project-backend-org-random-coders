package org.example.vet1177.services;

import org.example.vet1177.dto.request.pet.PetRequest;
import org.example.vet1177.entities.Clinic;
import org.example.vet1177.entities.Pet;
import org.example.vet1177.entities.Role;
import org.example.vet1177.entities.User;
import org.example.vet1177.policy.PetPolicy;
import org.example.vet1177.repository.MedicalRecordRepository;
import org.example.vet1177.repository.PetRepository;
import org.example.vet1177.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
public class PetServiceTest {

    @Mock
    private PetRepository petRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PetPolicy petPolicy;

    @Mock
    private MedicalRecordRepository medicalRecordRepository;

    @InjectMocks
    private PetService petService;

    private User admin;
    private User owner;
    private User otherOwner;
    private User vet;
    private Clinic clinic;
    private Pet pet;
    private PetRequest petRequest;
    private UUID adminId;
    private UUID ownerId;
    private UUID otherOwnerId;
    private UUID vetId;
    private UUID petId;

    @BeforeEach
    void setUp( ) throws Exception{
        adminId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        otherOwnerId = UUID.randomUUID();
        vetId = UUID.randomUUID();
        petId = UUID.randomUUID();

        admin = new User("Admin Adminsson", "Admin@example.se", "lösenord123", Role.ADMIN);
        setPrivateField(admin, "id", adminId);
        owner = new User("Ägare Ägarsson", "Ägerettdjur@example.se", "ägarlösen123", Role.OWNER);
        setPrivateField(owner,"id", ownerId);
        otherOwner = new User("Annan Ägarsson", "annanägare@example.se", "lös123", Role.OWNER);
        setPrivateField(otherOwner,"id", otherOwnerId);
        vet = new User("Vet Vetisson", "vet@example.se", "lösenordet123", Role.VET);
        setPrivateField(vet,"id", vetId);
        clinic = new Clinic("Huvudkliniken", "storgatan 1", "+465872159125");
        setPrivateField(clinic,"id", UUID.randomUUID());
        pet = new Pet(owner, "Molly", "Hund", "Labrador", LocalDate.of(2020, 1, 1), new BigDecimal("12.50"));
        setPrivateField(pet, "id", petId);

        petRequest = new PetRequest();
        petRequest.setName("Harry");
        petRequest.setSpecies("Hund");
        petRequest.setBreed("labrador");
        petRequest.setDateOfBirth(LocalDate.of(2020,1, 1));
        petRequest.setWeightKg(new BigDecimal("12.50"));

    }


    //helper
    private static void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
