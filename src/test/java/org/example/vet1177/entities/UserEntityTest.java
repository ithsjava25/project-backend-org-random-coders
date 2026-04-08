package org.example.vet1177.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class UserEntityTest {
    private User user;

    @BeforeEach
    void setUp() {
        user = new User("Frida Svensson", "Frida@example.se", "lösenord!", Role.OWNER);
    }

    //kontrollerar att fält är null (eller true för isActive)

    @Test
    void getId_shouldBeNullBeforePersist() {
        assertThat(user.getId()).isNull();
    }

    @Test
    void isActive_shouldBeTrueByDefault() {
        assertThat(user.isActive()).isTrue();
    }

    @Test
    void getClinic_shouldBeNullWhenNotSet() {
        assertThat(user.getClinic()).isNull();
    }

    @Test
    void getCreatedAt_shouldBeNullBeforeOnCreate() {
        assertThat(user.getCreatedAt()).isNull();
    }

    @Test
    void getUpdatedAt_shouldBeNullBeforeOnCreate() {
        assertThat(user.getUpdatedAt()).isNull();
    }



}
