package org.example.vet1177.policy;

import org.example.vet1177.entities.Role;
import org.example.vet1177.entities.User;
import org.example.vet1177.exception.ForbiddenException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

public class AdminPolicyTest {

    private AdminPolicy policy;

    private User admin;
    private User owner;
    private User vet;

    @BeforeEach
    void setup(){
        policy = new AdminPolicy();

        admin = new User("Admin Adminsson", "Admin@example.se", "lösen!", Role.ADMIN);
        owner = new User("Ägare Andersson", "ägare@example.se", "lös123", Role.OWNER  );
        vet = new User("Vet vetrinär", "vetrinär@example.se", "lösenord42", Role.VET);
    }

    // RequireAdmin

    @Test
    void requireAdmin_admin_shouldNotThrow(){
        assertThatNoException().isThrownBy(()-> policy.requireAdmin(admin));
    }

    @Test
    void requireAdmin_owner_shouldThrowForbiddenException() {
        assertThatThrownBy(() -> policy.requireAdmin(owner))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Åtkomst nekad: Endast administratörer har behörighet");
    }

    @Test
    void requireAdmin_vet_shouldThrowForbiddenException() {
        assertThatThrownBy(() -> policy.requireAdmin(vet))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Åtkomst nekad: Endast administratörer har behörighet");
    }

    @Test
    void requireAdmin_nullUser_shouldThrowForbiddenException() {
        assertThatThrownBy(() -> policy.requireAdmin(null))
                .isInstanceOf(ForbiddenException.class)
                .hasMessage("Åtkomst nekad: Endast administratörer har behörighet");
    }

}
