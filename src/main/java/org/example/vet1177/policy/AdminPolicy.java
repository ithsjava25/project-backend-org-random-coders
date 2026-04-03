package org.example.vet1177.policy;

import org.example.vet1177.entities.Role;
import org.example.vet1177.entities.User;
import org.example.vet1177.exception.ForbiddenException;
import org.springframework.stereotype.Component;

@Component
public class AdminPolicy {

    public void requireAdmin(User user){
        if (user.getRole() != Role.ADMIN) {
            throw new ForbiddenException("Åtkomst nekad: Endast administratörer har behörighet");
        }
    }
}
