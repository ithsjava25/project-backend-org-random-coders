package org.example.vet1177.policy;

import org.example.vet1177.entities.*;
import org.example.vet1177.exception.BusinessRuleException;
import org.example.vet1177.exception.ForbiddenException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class MedicalRecordPolicy {

    public void canView(User user, MedicalRecord record) {
        switch (user.getRole()) {
            case OWNER -> {
                if (!user.getId().equals(record.getOwner().getId()))
                    throw new ForbiddenException("Du har inte tillgång till detta ärende");
            }
            case VET -> {
                if (!sameClinic(user, record))
                    throw new ForbiddenException("Du har inte tillgång till ärenden på en annan klinik");
            }
            case ADMIN -> {}
        }
    }

    public void canCreate(User user, Pet pet, Clinic clinic) {
        switch (user.getRole()) {
            case OWNER -> {
                if (!user.getId().equals(pet.getOwner().getId()))
                    throw new ForbiddenException("Du kan inte skapa ärende för någon annans djur");
            }
            case VET -> {
                if (!user.getClinic().getId().equals(clinic.getId()))
                    throw new ForbiddenException("Du kan inte skapa ärende för en annan klinik");
            }
            case ADMIN -> {}
        }
    }

    public void canUpdate(User user, MedicalRecord record) {
        if (record.getStatus().isFinal())
            throw new BusinessRuleException("Stängda ärenden kan inte uppdateras");

        switch (user.getRole()) {
            case OWNER -> {
                if (!user.getId().equals(record.getOwner().getId()))
                    throw new ForbiddenException("Du har inte tillgång till detta ärende");
            }
            case VET -> {
                if (!sameClinic(user, record))
                    throw new ForbiddenException("Du har inte tillgång till ärenden på en annan klinik");
            }
            case ADMIN -> {}
        }
    }

    public void canUpdateStatus(User user, MedicalRecord record, RecordStatus newStatus) {
        if (record.getStatus().isFinal())
            throw new BusinessRuleException("Stängda ärenden kan inte uppdateras");

        switch (user.getRole()) {
            case OWNER ->
                    throw new ForbiddenException("Ägare får inte ändra status på ärenden");
            case VET -> {
                if (!sameClinic(user, record))
                    throw new ForbiddenException("Du har inte tillgång till ärenden på en annan klinik");
            }
            case ADMIN -> {}
        }
    }

    public void canAssignVet(User user, MedicalRecord record, User vetToAssign) {
        switch (user.getRole()) {
            case OWNER ->
                    throw new ForbiddenException("Ägare får inte tilldela handläggare");
            case VET -> {
                if (!sameClinic(user, record))
                    throw new ForbiddenException("Du har inte tillgång till ärenden på en annan klinik");
                if (!sameClinicAsRecord(vetToAssign, record))
                    throw new ForbiddenException("Kan inte tilldela vet från en annan klinik");
            }
            case ADMIN -> {}
        }
    }

    public void canClose(User user, MedicalRecord record) {
        if (record.getStatus().isFinal())
            throw new BusinessRuleException("Ärendet är redan stängt");

        switch (user.getRole()) {
            case OWNER ->
                    throw new ForbiddenException("Ägare får inte stänga ärenden");
            case VET -> {
                if (!sameClinic(user, record))
                    throw new ForbiddenException("Du har inte tillgång till ärenden på en annan klinik");
            }
            case ADMIN -> {}
        }
    }

    public void canViewClinic(User user, UUID clinicId) {
        switch (user.getRole()) {
            case OWNER ->
                    throw new ForbiddenException("Ägare har inte tillgång till klinikvy");
            case VET -> {
                if (!user.getClinic().getId().equals(clinicId))
                    throw new ForbiddenException("Du har inte tillgång till en annan kliniqs ärenden");
            }
            case ADMIN -> {}
        }
    }

    // ── Hjälpmetoder ─────────────────────────────────────────

    private boolean sameClinic(User user, MedicalRecord record) {
        return user.getClinic() != null &&
                user.getClinic().getId().equals(record.getClinic().getId());
    }

    private boolean sameClinicAsRecord(User vet, MedicalRecord record) {
        return vet.getClinic() != null &&
                vet.getClinic().getId().equals(record.getClinic().getId());
    }
}