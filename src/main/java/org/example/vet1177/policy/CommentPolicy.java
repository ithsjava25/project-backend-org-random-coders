package org.example.vet1177.policy;

import org.example.vet1177.entities.*;
import org.example.vet1177.exception.BusinessRuleException;
import org.example.vet1177.exception.ForbiddenException;
import org.springframework.stereotype.Component;

@Component
public class CommentPolicy {

    public void canCreate(User user, MedicalRecord record) {

        if (record.getStatus().isFinal()) {
            throw new BusinessRuleException("Stängda ärenden kan inte kommenteras");
        }

        switch (user.getRole()) {
            case OWNER -> {
                if (!user.getId().equals(record.getOwner().getId()))
                    throw new ForbiddenException("Du kan inte kommentera på någon annans ärende");
            }
            case VET -> {
                if (!user.getClinic().getId().equals(record.getClinic().getId()))
                    throw new ForbiddenException("Du kan inte kommentera ärenden på en annan klinik");
            }
            case ADMIN -> {}
        }
    }

    public void canView(User user, MedicalRecord record) {
        switch (user.getRole()) {
            case OWNER -> {
                if (!user.getId().equals(record.getOwner().getId()))
                    throw new ForbiddenException("Åtkomst nekad");
            }
            case VET -> {
                if (!user.getClinic().getId().equals(record.getClinic().getId()))
                    throw new ForbiddenException("Åtkomst nekad");
            }
            case ADMIN -> {}
        }
    }

    public void canUpdate(User user, Comment comment) {

        if (comment.getMedicalRecord().getStatus().isFinal()) {
            throw new BusinessRuleException("Kommentarer på stängda ärenden kan inte redigeras");
        }

        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new ForbiddenException("Du kan bara redigera dina egna kommentarer");
        }
    }

    public void canDelete(User user, Comment comment) {
        if (user.getRole() != Role.ADMIN &&
                !comment.getAuthor().getId().equals(user.getId())) {
            throw new ForbiddenException("Du kan bara ta bort dina egna kommentarer");
        }
    }
}