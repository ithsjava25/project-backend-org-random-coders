package org.example.vet1177.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class PetEntityTest {
    private Pet pet;
    private User owner;

    @BeforeEach
    void setUp(){
        owner = new User("Kalle Karlsson", "kalle.k@example.se", "hasg123", Role.OWNER);
        pet = new Pet();
    }
    //Getters & setters (name, species, breed, dateOfBirth, weightKg, owner)
    @Test
    void setName_shouldStoreAndReturnCorrectValue(){
        pet.setName("Harry");

        assertThat(pet.getName()).isEqualTo("Harry");
    }

    @Test
    void setSpeices_shouldStoreAndReturnCorrectValue(){
        pet.setSpecies("hund");

        assertThat(pet.getSpecies()).isEqualTo("hund");
    }


    @Test
    void setBread_shouldStoreAndReturnCorrectValue(){
        pet.setBreed("Labrador");

        assertThat(pet.getBreed()).isEqualTo("Labrador");
    }

    @Test
    void setDateOfBirth_shouldStorAndReturnCorrectValue(){
        LocalDate dob = LocalDate.of(2025, 04, 02);
        pet.setDateOfBirth(dob);

        assertThat(pet.getDateOfBirth()).isEqualTo(dob);
    }

    @Test
    void setWeigthKg_shouldStoreAndReturnCorrectValue(){
        BigDecimal weight = new BigDecimal(13.50);
        pet.setWeightKg(weight);

        assertThat(pet.getWeightKg()).isEqualByComparingTo(weight);
    }

    @Test
    void setOwner_ShouldStoreAndReturnCorrectUser(){
        pet.setOwner(owner);
        assertThat(pet.getOwner()).isSameAs(owner);
    }

    //Konstruktorn med alla fält
    @Test
    void fullConstructor_shouldSetAllFields(){
        LocalDate dob = LocalDate.of(2025,03,03);
        BigDecimal weight = new BigDecimal(12.20);

        Pet constructed = new Pet(owner, "Harry", "hund", "pudel", dob, weight);

        assertThat(constructed.getOwner()).isSameAs(owner);
        assertThat(constructed.getName()).isEqualTo("Harry");
        assertThat(constructed.getSpecies()).isEqualTo("hund");
        assertThat(constructed.getBreed()).isEqualTo("pudel");
        assertThat(constructed.getDateOfBirth()).isEqualTo(dob);
        assertThat(constructed.getWeightKg()).isEqualByComparingTo(weight);
    }

    @Test
    void fullConstructor_shouldAllowNullBreed(){
        Pet constructed = new Pet(owner, "Max", "Katt", null, LocalDate.now(), new BigDecimal("5.00"));
        assertThat(constructed.getBreed()).isNull();
    }

    //onCreate() sätter createdAt och updatedAt


    //onUpdate() uppdaterar updatedAt men rör inte createdAt


    //Standardvärden är null innan persist


}
