package org.example.vet1177.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;

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

    //livscykel: onCreate
    @Test
    void onCreate_shouldSetBothTimestampsToNonNull() {
        user.onCreate();

        assertThat(user.getCreatedAt()).isNotNull();
        assertThat(user.getUpdatedAt()).isNotNull();
    }

    @Test
    void onCreate_shouldSetTimestampsCloseToNow() {
        Instant before = Instant.now();
        user.onCreate();
        Instant after = Instant.now();

        assertThat(user.getCreatedAt()).isBetween(before, after);
        assertThat(user.getUpdatedAt()).isBetween(before, after);
    }

    //livscykel på onUpdate
    @Test
    void onUpdate_shouldRefreshUpdatedAt() {
        user.onCreate();

        user.onUpdate();

        assertThat(user.getUpdatedAt()).isNotNull();
    }

    @Test
    void onUpdate_shouldNotModifyCreatedAt() {
        user.onCreate();
        Instant originalCreatedAt = user.getCreatedAt();

        user.onUpdate();

        assertThat(user.getCreatedAt()).isEqualTo(originalCreatedAt);
    }

}
