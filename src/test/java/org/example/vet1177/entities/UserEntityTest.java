package org.example.vet1177.entities;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    //UserDetails

    @Test
    void getUsername_shouldReturnEmail() {
        assertThat(user.getUsername()).isEqualTo("Frida@example.se");
    }

    @Test
    void getPassword_shouldReturnPasswordHash() {
        assertThat(user.getPassword()).isEqualTo("lösenord!");
    }

    @Test
    void isEnabled_shouldReturnTrueWhenActive() {
        assertThat(user.isEnabled()).isTrue();
    }

    @Test
    void isEnabled_shouldReturnFalseWhenInactive() {
        user.setActive(false);

        assertThat(user.isEnabled()).isFalse();
    }

    @Test
    void isAccountNonLocked_shouldReturnTrueWhenActive() {
        assertThat(user.isAccountNonLocked()).isTrue();
    }

    @Test
    void isAccountNonLocked_shouldReturnFalseWhenInactive() {
        user.setActive(false);

        assertThat(user.isAccountNonLocked()).isFalse();
    }

    //GetAuthorties
    @Test
    void getAuthorities_ownerRole_shouldReturnRoleOwner() {
        User owner = new User("Anna", "anna@mail.se", "hash", Role.OWNER);
        Collection<? extends GrantedAuthority> authorities = owner.getAuthorities();

        assertThat(new ArrayList<>(authorities))
                .extracting(a -> a.getAuthority())
                .containsExactly("ROLE_OWNER");
    }

    @Test
    void getAuthorities_vetRole_shouldReturnRoleVet() {
        User vet = new User("Dr. Erik", "erik@vet.se", "hash", Role.VET);
        Collection<? extends GrantedAuthority> authorities = vet.getAuthorities();

        assertThat(new ArrayList<>(authorities))
                .extracting(a -> a.getAuthority())
                .containsExactly("ROLE_VET");
    }

    @Test
    void getAuthorities_adminRole_shouldReturnRoleAdmin() {
        User admin = new User("Admin", "admin@vet.se", "hash", Role.ADMIN);
        Collection<? extends GrantedAuthority> authorities = admin.getAuthorities();

        assertThat(new ArrayList<>(authorities))
                .extracting(a -> a.getAuthority())
                .containsExactly("ROLE_ADMIN");
    }

    //addAttachment

    @Test
    void addAttachment_shouldAddToList() {
        Attachment attachment = new Attachment();

        user.addAttachment(attachment);

        assertThat(user.getUploadedAttachments()).contains(attachment);
    }

    @Test
    void addAttachment_shouldSetUploadedByOnAttachment() {
        Attachment attachment = new Attachment();

        user.addAttachment(attachment);

        assertThat(attachment.getUploadedBy()).isSameAs(user);
    }

    @Test
    void addAttachment_nullAttachment_shouldNotAddToList() {
        user.addAttachment(null);

        assertThat(user.getUploadedAttachments()).isEmpty();
    }

    // removeAttachment

    @Test
    void removeAttachment_shouldRemoveFromList() {
        Attachment attachment = new Attachment();
        user.addAttachment(attachment);

        user.removeAttachment(attachment);

        assertThat(user.getUploadedAttachments()).doesNotContain(attachment);
    }

    @Test
    void removeAttachment_shouldSetUploadedByToNull() {
        Attachment attachment = new Attachment();
        user.addAttachment(attachment);

        user.removeAttachment(attachment);

        assertThat(attachment.getUploadedBy()).isNull();
    }

    @Test
    void removeAttachment_nullAttachment_shouldNotThrow() {
        user.addAttachment(new Attachment());

        user.removeAttachment(null);

        assertThat(user.getUploadedAttachments()).hasSize(1);
    }
    // getUploadedAttachments

    @Test
    void getUploadedAttachments_shouldReturnUnmodifiableList() {
        Attachment attachment = new Attachment();
        user.addAttachment(attachment);

        assertThatThrownBy(() -> user.getUploadedAttachments().add(new Attachment()))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void getUploadedAttachments_shouldBeEmptyByDefault() {
        assertThat(user.getUploadedAttachments()).isEmpty();
    }
}
